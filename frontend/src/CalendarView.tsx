import viLocale from "@fullcalendar/core/locales/vi";
import interactionPlugin from "@fullcalendar/interaction";
import FullCalendar from "@fullcalendar/react";
import timeGridPlugin from "@fullcalendar/timegrid";
import { useEffect, useState } from "react";
import AppointmentModal from "./AppointmentModal";
import ConflictWarning from "./ConflictWarning";

export default function CalendarView({ onLogout }: { onLogout: () => void }) {
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
  const [events, setEvents] = useState<any[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<any>(null);
  const [conflictType, setConflictType] = useState<string | null>(null);
  const [conflictMessage, setConflictMessage] = useState("");
  const [conflictDetails, setConflictDetails] = useState<any>(null);
  const [pendingAppointment, setPendingAppointment] = useState<any>(null);
  const [culture, setCulture] = useState("en-US");
  const [selectedAppointment, setSelectedAppointment] = useState<any>(null);

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const token = localStorage.getItem("jwt");
      const response = await fetch(`${apiBaseUrl}/api/appointments`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.status === 401) {
        onLogout();
        return;
      }
      const data = await response.json();
      const formattedData = data.map((appt: any) => ({
        id: appt.id,
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
      }));
      setEvents(formattedData);
    } catch (error) {
      console.error("Failed to fetch appointments", error);
    }
  };

  const handleSelectSlot = (selectInfo: any) => {
    setSelectedSlot({
      start: selectInfo.start,
      end: selectInfo.end,
    });
    // Unselect the internal selection immediately so it doesn't linger visually after modal interactions
    selectInfo.view.calendar.unselect();
    setIsModalOpen(true);
  };

  const handleEventClick = (clickInfo: any) => {
    const appointmentId = clickInfo.event.extendedProps.id;
    fetchAppointmentDetails(appointmentId);
  };

  const fetchAppointmentDetails = async (id: number) => {
    try {
      const token = localStorage.getItem("jwt");
      const response = await fetch(`${apiBaseUrl}/api/appointments/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.status === 401) {
        onLogout();
        return;
      }
      if (!response.ok) {
        alert("Failed to fetch appointment details");
        return;
      }
      const data = await response.json();
      setSelectedAppointment(data);
    } catch (error) {
      console.error("Failed to fetch appointment details", error);
    }
  };

  const handleSaveAppointment = async (
    appointmentData: any,
    forceReplace = false,
    forceJoin = false,
  ) => {
    try {
      const token = localStorage.getItem("jwt");

      // For group meetings, skip validation and create directly
      if (appointmentData.appointmentType === "group") {
        const endpoint = `${apiBaseUrl}/api/appointments/group`;
        const requestBody: any = {
          name: appointmentData.name,
          location: appointmentData.location,
          startTime: appointmentData.startTime,
          endTime: appointmentData.endTime,
          reminderMinutes: appointmentData.reminderMinutes,
        };

        if (appointmentData.participantUsernames) {
          requestBody.participantUsernames =
            appointmentData.participantUsernames
              .split(",")
              .map((u: string) => u.trim())
              .filter((u: string) => u.length > 0);
        }

        const response = await fetch(endpoint, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(requestBody),
        });

        if (response.status === 401) {
          onLogout();
          return;
        }

        if (!response.ok) {
          const msg = await response.json();
          alert("Error: " + (msg.message || "Invalid appointment"));
          return;
        }

        setIsModalOpen(false);
        setConflictType(null);
        setPendingAppointment(null);
        fetchAppointments();
        return;
      }

      // For normal appointments: Step 1 - Validate (only if not forcing replace or join)
      if (!forceReplace && !forceJoin) {
        const validateResponse = await fetch(
          `${apiBaseUrl}/api/appointments/validate`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              name: appointmentData.name,
              startTime: appointmentData.startTime,
              endTime: appointmentData.endTime,
            }),
          },
        );

        if (validateResponse.status === 401) {
          onLogout();
          return;
        }

        if (!validateResponse.ok) {
          const msg = await validateResponse.json();
          alert("Error: " + (msg.message || "Validation failed"));
          return;
        }

        const validationResult = await validateResponse.json();

        // Handle conflicts
        if (validationResult.conflictType === "TIME_OVERLAP") {
          setConflictType("OVERLAP");
          setConflictMessage(validationResult.message);
          setConflictDetails(validationResult.details);
          setPendingAppointment(appointmentData);
          return;
        }

        if (validationResult.conflictType === "GROUP_MEETING_MATCH") {
          setConflictType("GROUP_MEETING");
          setConflictMessage(validationResult.message);
          setConflictDetails(validationResult.details);
          setPendingAppointment(appointmentData);
          return;
        }
      }

      // Step 2 - Create appointment
      const requestBody: any = {
        name: appointmentData.name,
        location: appointmentData.location,
        startTime: appointmentData.startTime,
        endTime: appointmentData.endTime,
        reminderMinutes: appointmentData.reminderMinutes,
        forceReplace: forceReplace,
        forceJoin: forceJoin,
      };

      const response = await fetch(`${apiBaseUrl}/api/appointments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(requestBody),
      });

      if (response.status === 401) {
        onLogout();
        return;
      }

      if (!response.ok) {
        const msg = await response.json();
        alert("Error: " + (msg.message || "Invalid appointment"));
        return;
      }

      setIsModalOpen(false);
      setConflictType(null);
      setConflictDetails(null);
      setPendingAppointment(null);
      fetchAppointments();
    } catch (error) {
      console.error("Failed to save appointment", error);
    }
  };

  const handleDeleteAppointment = async (id: number) => {
    if (!window.confirm("Are you sure you want to delete this appointment?")) {
      return;
    }
    try {
      const token = localStorage.getItem("jwt");

      const response = await fetch(`${apiBaseUrl}/api/appointments/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401) {
        onLogout();
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
        {/* .fc-event: block appointment */}
        <style>{`
          .fc-event {
            cursor: pointer;
          }
          .fc-event:hover {
            opacity: 0.8;
          }
        `}</style>
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
          onSave={(data: any) => handleSaveAppointment(data)}
        />
      )}

      {conflictType && (
        <ConflictWarning
          type={conflictType}
          message={conflictMessage}
          details={conflictDetails}
          onCancel={() => {
            setConflictType(null);
            setConflictDetails(null);
          }}
          onReplace={() =>
            handleSaveAppointment(pendingAppointment, true, false)
          }
          onJoin={() => handleSaveAppointment(pendingAppointment, false, true)}
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
                <label className="block text-sm font-medium text-gray-600">
                  Tên:
                </label>
                <p className="text-gray-900">{selectedAppointment.name}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Địa điểm:
                </label>
                <p className="text-gray-900">
                  {selectedAppointment.location || "N/A"}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Thời gian bắt đầu:
                </label>
                <p className="text-gray-900">
                  {new Date(selectedAppointment.startTime).toLocaleString(
                    "vi-VN",
                  )}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Thời gian kết thúc:
                </label>
                <p className="text-gray-900">
                  {new Date(selectedAppointment.endTime).toLocaleString(
                    "vi-VN",
                  )}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Loại:
                </label>
                <p className="text-gray-900">
                  {selectedAppointment.appointmentType === "GROUP_MEETING"
                    ? "Cuộc họp nhóm"
                    : "Cuộc hẹn cá nhân"}
                </p>
              </div>
              {selectedAppointment.appointmentType === "GROUP_MEETING" &&
                selectedAppointment.participants && (
                  <div>
                    <label className="block text-sm font-medium text-gray-600">
                      Người tham gia:
                    </label>
                    <ul className="text-gray-900 list-disc list-inside">
                      {selectedAppointment.participants.map(
                        (participant: string, index: number) => (
                          <li key={index}>{participant}</li>
                        ),
                      )}
                    </ul>
                  </div>
                )}
              {selectedAppointment.reminderMinutes !== null && (
                <div>
                  <label className="block text-sm font-medium text-gray-600">
                    Nhắc nhở trước:
                  </label>
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
