import type {
  DateSelectArg,
  EventClickArg,
  EventInput,
} from "@fullcalendar/core";
import viLocale from "@fullcalendar/core/locales/vi";
import interactionPlugin from "@fullcalendar/interaction";
import FullCalendar from "@fullcalendar/react";
import timeGridPlugin from "@fullcalendar/timegrid";
import { useCallback, useEffect, useState } from "react";
import type { AppointmentFormData, AppointmentSlot } from "./AppointmentModal";
import AppointmentModal from "./AppointmentModal";
import type { ConflictDetails, ConflictType } from "./ConflictWarning";
import ConflictWarning from "./ConflictWarning";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const CALENDAR_EVENT_STYLES = `
  .fc-event {
    cursor: pointer;
  }
  .fc-event:hover {
    opacity: 0.8;
  }
`;

type ApiAppointment = {
  id: number;
  name: string;
  location: string;
  appointmentType: string;
  isGroupMeeting: boolean;
  startTime: string;
  endTime: string;
};

type AppointmentDetails = {
  id: number;
  name: string;
  location?: string | null;
  appointmentType: string;
  startTime: string;
  endTime: string;
  participants?: string[];
  reminderMinutes?: number | null;
};

type ValidationConflictType = "TIME_OVERLAP" | "GROUP_MEETING_MATCH";

type ValidationResult = {
  conflictType?: ValidationConflictType;
  message?: string;
  details?: ConflictDetails;
};

type Culture = "en-US" | "vi";

type CalendarViewProps = Readonly<{
  onLogout: () => void;
}>;

const buildAuthHeaders = (headers?: HeadersInit) => {
  const token = localStorage.getItem("jwt");
  const mergedHeaders = new Headers(headers);
  mergedHeaders.set("Authorization", `Bearer ${token}`);
  return mergedHeaders;
};

const fetchWithAuth = (url: string, init: RequestInit = {}) =>
  fetch(url, {
    ...init,
    headers: buildAuthHeaders(init.headers),
  });

const toCalendarEvent = (appt: ApiAppointment): EventInput => ({
  id: String(appt.id),
  start: new Date(appt.startTime),
  end: new Date(appt.endTime),
  title: appt.name + (appt.isGroupMeeting ? " (Group)" : ""),
  extendedProps: {
    id: appt.id,
    name: appt.name,
    location: appt.location,
    appointmentType: appt.appointmentType,
    isGroupMeeting: appt.isGroupMeeting,
  },
});

export default function CalendarView({ onLogout }: CalendarViewProps) {
  const [events, setEvents] = useState<EventInput[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<AppointmentSlot | null>(
    null,
  );
  const [conflictType, setConflictType] = useState<ConflictType | null>(null);
  const [conflictMessage, setConflictMessage] = useState("");
  const [conflictDetails, setConflictDetails] =
    useState<ConflictDetails | null>(null);
  const [pendingAppointment, setPendingAppointment] =
    useState<AppointmentFormData | null>(null);
  const [culture, setCulture] = useState<Culture>("en-US");
  const [selectedAppointment, setSelectedAppointment] =
    useState<AppointmentDetails | null>(null);

  const handleUnauthorized = useCallback(
    (response: Response) => {
      if (response.status !== 401) {
        return false;
      }
      onLogout();
      return true;
    },
    [onLogout],
  );

  const fetchAppointments = useCallback(async () => {
    try {
      const response = await fetchWithAuth(`${API_BASE_URL}/api/appointments`);
      if (handleUnauthorized(response)) {
        return;
      }
      const data = (await response.json()) as ApiAppointment[];
      setEvents(data.map(toCalendarEvent));
    } catch (error) {
      console.error("Failed to fetch appointments", error);
    }
  }, [handleUnauthorized]);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      void fetchAppointments();
    }, 0);
    return () => clearTimeout(timeoutId);
  }, [fetchAppointments]);

  const handleSelectSlot = (selectInfo: DateSelectArg) => {
    setSelectedSlot({
      start: selectInfo.start,
      end: selectInfo.end,
    });
    selectInfo.view.calendar.unselect();
    setIsModalOpen(true);
  };

  const handleEventClick = (clickInfo: EventClickArg) => {
    const appointmentId = Number(clickInfo.event.extendedProps.id);
    if (Number.isNaN(appointmentId)) {
      return;
    }
    fetchAppointmentDetails(appointmentId);
  };

  const fetchAppointmentDetails = async (id: number) => {
    try {
      const response = await fetchWithAuth(
        `${API_BASE_URL}/api/appointments/${id}`,
      );
      if (handleUnauthorized(response)) {
        return;
      }
      if (!response.ok) {
        alert("Failed to fetch appointment details");
        return;
      }
      const data = (await response.json()) as AppointmentDetails;
      setSelectedAppointment(data);
    } catch (error) {
      console.error("Failed to fetch appointment details", error);
    }
  };

  const validateAppointment = async (appointmentData: AppointmentFormData) => {
    const response = await fetchWithAuth(
      `${API_BASE_URL}/api/appointments/validate`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name: appointmentData.name,
          startTime: appointmentData.startTime,
          endTime: appointmentData.endTime,
        }),
      },
    );

    if (handleUnauthorized(response)) {
      return null;
    }

    if (!response.ok) {
      const msg = await response.json();
      alert("Error: " + (msg.message || "Validation failed"));
      return null;
    }

    return (await response.json()) as ValidationResult;
  };

  const queueConflict = (
    type: ConflictType,
    result: ValidationResult,
    appointmentData: AppointmentFormData,
  ) => {
    setConflictType(type);
    setConflictMessage(result.message ?? "");
    setConflictDetails(result.details ?? null);
    setPendingAppointment(appointmentData);
  };

  const clearConflict = () => {
    setConflictType(null);
    setConflictDetails(null);
    setPendingAppointment(null);
  };

  const saveGroupMeeting = async (
    appointmentData: AppointmentFormData,
    forceReplace: boolean,
    forceJoin: boolean,
  ) => {
    if (!forceReplace) {
      const validationResult = await validateAppointment(appointmentData);
      if (!validationResult) {
        return false;
      }

      if (validationResult.conflictType === "GROUP_MEETING_MATCH") {
        queueConflict("GROUP_MEETING", validationResult, appointmentData);
        return false;
      }
    }

    const endpoint = `${API_BASE_URL}/api/appointments/group`;
    const requestBody: Record<string, unknown> = {
      name: appointmentData.name,
      location: appointmentData.location,
      startTime: appointmentData.startTime,
      endTime: appointmentData.endTime,
      reminderMinutes: appointmentData.reminderMinutes,
      forceReplace: forceReplace,
      forceJoin: forceJoin,
    };

    if (appointmentData.participantUsernames) {
      requestBody.participantUsernames = appointmentData.participantUsernames
        .split(",")
        .map((username) => username.trim())
        .filter((username) => username.length > 0);
    }

    const response = await fetchWithAuth(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    if (handleUnauthorized(response)) {
      return false;
    }

    if (!response.ok) {
      const msg = await response.json();
      alert("Error: " + (msg.message || "Invalid appointment"));
      return false;
    }

    return true;
  };

  const saveNormalAppointment = async (
    appointmentData: AppointmentFormData,
    forceReplace: boolean,
    forceJoin: boolean,
  ) => {
    if (!forceReplace && !forceJoin) {
      const validationResult = await validateAppointment(appointmentData);
      if (!validationResult) {
        return false;
      }

      if (validationResult.conflictType === "TIME_OVERLAP") {
        queueConflict("OVERLAP", validationResult, appointmentData);
        return false;
      }

      if (validationResult.conflictType === "GROUP_MEETING_MATCH") {
        queueConflict("GROUP_MEETING", validationResult, appointmentData);
        return false;
      }
    }

    const requestBody: Record<string, unknown> = {
      name: appointmentData.name,
      location: appointmentData.location,
      startTime: appointmentData.startTime,
      endTime: appointmentData.endTime,
      reminderMinutes: appointmentData.reminderMinutes,
      forceReplace: forceReplace,
      forceJoin: forceJoin,
    };

    const response = await fetchWithAuth(`${API_BASE_URL}/api/appointments`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    if (handleUnauthorized(response)) {
      return false;
    }

    if (!response.ok) {
      const msg = await response.json();
      alert("Error: " + (msg.message || "Invalid appointment"));
      return false;
    }

    return true;
  };

  const handleSaveAppointment = async (
    appointmentData: AppointmentFormData,
    forceReplace = false,
    forceJoin = false,
  ) => {
    try {
      const isGroupMeeting = appointmentData.appointmentType === "group";
      const didSave = isGroupMeeting
        ? await saveGroupMeeting(appointmentData, forceReplace, forceJoin)
        : await saveNormalAppointment(appointmentData, forceReplace, forceJoin);

      if (!didSave) {
        return;
      }

      setIsModalOpen(false);
      clearConflict();
      fetchAppointments();
    } catch (error) {
      console.error("Failed to save appointment", error);
    }
  };

  const handleDeleteAppointment = async (id: number) => {
    if (
      !globalThis.confirm("Are you sure you want to delete this appointment?")
    ) {
      return;
    }
    try {
      const response = await fetchWithAuth(
        `${API_BASE_URL}/api/appointments/${id}`,
        {
          method: "DELETE",
        },
      );

      if (handleUnauthorized(response)) {
        return;
      }

      if (!response.ok) {
        const msg = await response.json();
        alert("Error: " + (msg.message || "Failed to delete appointment"));
        return;
      }

      setSelectedAppointment(null);
      fetchAppointments();
    } catch (error) {
      console.error("Failed to delete appointment", error);
    }
  };

  return (
    <div className="p-4 bg-gray-50 min-h-screen">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">My Calendar</h1>
        <div className="flex space-x-2">
          <button
            onClick={() => setCulture("en-US")}
            className={`px-3 py-1 rounded text-sm font-medium transition ${culture === "en-US" ? "bg-blue-600 text-white" : "bg-gray-200 text-gray-700 hover:bg-gray-300"}`}
          >
            English
          </button>
          <button
            onClick={() => setCulture("vi")}
            className={`px-3 py-1 rounded text-sm font-medium transition ${culture === "vi" ? "bg-blue-600 text-white" : "bg-gray-200 text-gray-700 hover:bg-gray-300"}`}
          >
            Tiếng Việt
          </button>
          <button
            onClick={onLogout}
            className="px-3 py-1 rounded text-sm font-medium transition bg-red-100 text-red-600 hover:bg-red-200 ml-4"
          >
            Logout
          </button>
        </div>
      </div>
      <div
        className="bg-white rounded-xl shadow p-4"
        style={{ height: "80vh" }}
      >
        <style>{CALENDAR_EVENT_STYLES}</style>
        <FullCalendar
          plugins={[timeGridPlugin, interactionPlugin]}
          initialView="timeGridWeek"
          headerToolbar={{
            left: "prev,next today",
            center: "title",
            right: "", // Just view by week, no extra view buttons
          }}
          selectable={true}
          select={handleSelectSlot}
          eventClick={handleEventClick}
          events={events}
          locale={culture === "vi" ? viLocale : undefined}
          height="100%"
          eventTimeFormat={{
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
          }}
          slotLabelFormat={{
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
          }}
        />
      </div>

      {isModalOpen && (
        <AppointmentModal
          slot={selectedSlot}
          onClose={() => setIsModalOpen(false)}
          onSave={handleSaveAppointment}
        />
      )}

      {conflictType && (
        <ConflictWarning
          type={conflictType}
          message={conflictMessage}
          details={conflictDetails}
          onCancel={clearConflict}
          onReplace={() =>
            pendingAppointment &&
            handleSaveAppointment(pendingAppointment, true, false)
          }
          onJoin={() =>
            pendingAppointment &&
            handleSaveAppointment(pendingAppointment, false, true)
          }
        />
      )}

      {selectedAppointment && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
            <h2 className="text-2xl font-bold mb-4 text-gray-800">
              Chi tiết cuộc hẹn
            </h2>
            <div className="space-y-3">
              <div>
                <p className="block text-sm font-medium text-gray-600">Tên:</p>
                <p className="text-gray-900">{selectedAppointment.name}</p>
              </div>
              <div>
                <p className="block text-sm font-medium text-gray-600">
                  Địa điểm:
                </p>
                <p className="text-gray-900">
                  {selectedAppointment.location || "N/A"}
                </p>
              </div>
              <div>
                <p className="block text-sm font-medium text-gray-600">
                  Thời gian bắt đầu:
                </p>
                <p className="text-gray-900">
                  {new Date(selectedAppointment.startTime).toLocaleString(
                    "vi-VN",
                  )}
                </p>
              </div>
              <div>
                <p className="block text-sm font-medium text-gray-600">
                  Thời gian kết thúc:
                </p>
                <p className="text-gray-900">
                  {new Date(selectedAppointment.endTime).toLocaleString(
                    "vi-VN",
                  )}
                </p>
              </div>
              <div>
                <p className="block text-sm font-medium text-gray-600">Loại:</p>
                <p className="text-gray-900">
                  {selectedAppointment.appointmentType === "GROUP_MEETING"
                    ? "Cuộc họp nhóm"
                    : "Cuộc hẹn cá nhân"}
                </p>
              </div>
              {selectedAppointment.appointmentType === "GROUP_MEETING" &&
                selectedAppointment.participants && (
                  <div>
                    <p className="block text-sm font-medium text-gray-600">
                      Người tham gia:
                    </p>
                    <ul className="text-gray-900 list-disc list-inside">
                      {selectedAppointment.participants.map(
                        (participant: string) => (
                          <li key={participant}>{participant}</li>
                        ),
                      )}
                    </ul>
                  </div>
                )}
              {selectedAppointment.reminderMinutes !== null && (
                <div>
                  <p className="block text-sm font-medium text-gray-600">
                    Nhắc nhở trước:
                  </p>
                  <p className="text-gray-900">
                    {selectedAppointment.reminderMinutes} phút
                  </p>
                </div>
              )}
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => handleDeleteAppointment(selectedAppointment.id)}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition cursor-pointer"
              >
                Xóa
              </button>
              <button
                onClick={() => setSelectedAppointment(null)}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition cursor-pointer"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
