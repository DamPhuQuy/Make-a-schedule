import React, { useState } from "react";

export type AppointmentType = "normal" | "group";

export type AppointmentSlot = {
  start: Date;
  end: Date;
};

export type AppointmentFormData = {
  appointmentType: AppointmentType;
  name: string;
  location: string;
  participantUsernames: string | null;
  startTime: string;
  endTime: string;
  reminderMinutes: number | null;
};

type AppointmentModalProps = Readonly<{
  slot?: AppointmentSlot | null;
  onClose: () => void;
  onSave: (data: AppointmentFormData) => void;
}>;

const toLocalInputValue = (date: Date) => {
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
};

const getInitialTime = (
  slot: AppointmentSlot | null | undefined,
  key: "start" | "end",
) => (slot ? toLocalInputValue(slot[key]) : "");

const REMINDER_OPTIONS = [
  { value: "0", label: "None" },
  { value: "15", label: "15 minutes before" },
  { value: "30", label: "30 minutes before" },
  { value: "60", label: "1 hour before" },
  { value: "120", label: "2 hour before" },
  { value: "1440", label: "1 day before" },
  { value: "2880", label: "2 day before" },
  { value: "10080", label: "1 week before" },
];

export default function AppointmentModal({
  slot,
  onClose,
  onSave,
}: AppointmentModalProps) {
  const [appointmentType, setAppointmentType] =
    useState<AppointmentType>("normal");
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [participantUsernames, setParticipantUsernames] = useState("");
  const [startTime, setStartTime] = useState(() =>
    getInitialTime(slot, "start"),
  );
  const [endTime, setEndTime] = useState(() => getInitialTime(slot, "end"));
  const [reminder, setReminder] = useState("0");
  const [error, setError] = useState("");

  const handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmedName = name.trim();
    if (!trimmedName) {
      setError("Name cannot be empty.");
      return;
    }

    const start = new Date(startTime);
    const end = new Date(endTime);
    if (end <= start) {
      setError(
        "End time must be securely after start time. Duration must be positive.",
      );
      return;
    }

    const isGroupMeeting = appointmentType === "group";

    onSave({
      appointmentType,
      name: trimmedName,
      location,
      participantUsernames: isGroupMeeting ? participantUsernames : null,
      startTime: start.toISOString(),
      endTime: end.toISOString(),
      reminderMinutes: reminder === "0" ? null : Number(reminder),
    });
  };

  const isGroupMeeting = appointmentType === "group";

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-xl w-96 relative">
        <h2 className="text-2xl font-bold mb-4">Add Appointment</h2>
        {error && (
          <div className="bg-red-100 text-red-700 p-2 rounded mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="appointment-type"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Appointment Type
            </label>
            <select
              id="appointment-type"
              className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
              value={appointmentType}
              onChange={(event) =>
                setAppointmentType(event.target.value as AppointmentType)
              }
            >
              <option value="normal">Normal Appointment</option>
              <option value="group">Group Meeting</option>
            </select>
          </div>
          <div>
            <label
              htmlFor="appointment-name"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Name
            </label>
            <input
              id="appointment-name"
              type="text"
              className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="e.g. Doctor Appointment"
            />
          </div>
          <div>
            <label
              htmlFor="appointment-location"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Location
            </label>
            <input
              id="appointment-location"
              type="text"
              className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
              value={location}
              onChange={(event) => setLocation(event.target.value)}
              placeholder="e.g. Office, Online"
            />
          </div>
          {isGroupMeeting && (
            <div>
              <label
                htmlFor="appointment-participants"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                Participant Usernames
              </label>
              <input
                id="appointment-participants"
                type="text"
                className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
                value={participantUsernames}
                onChange={(event) =>
                  setParticipantUsernames(event.target.value)
                }
                placeholder="e.g. user1, user2, user3"
              />
              <p className="text-xs text-gray-500 mt-1">
                Separate multiple usernames with commas
              </p>
            </div>
          )}
          <div className="flex space-x-2">
            <div className="w-1/2">
              <label
                htmlFor="appointment-start"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                Start Time
              </label>
              <input
                id="appointment-start"
                type="datetime-local"
                className="w-full border border-gray-300 rounded p-2"
                value={startTime}
                onChange={(event) => setStartTime(event.target.value)}
              />
            </div>
            <div className="w-1/2">
              <label
                htmlFor="appointment-end"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                End Time
              </label>
              <input
                id="appointment-end"
                type="datetime-local"
                className="w-full border border-gray-300 rounded p-2"
                value={endTime}
                onChange={(event) => setEndTime(event.target.value)}
              />
            </div>
          </div>
          <div>
            <label
              htmlFor="appointment-reminder"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Reminder
            </label>
            <select
              id="appointment-reminder"
              className="w-full border border-gray-300 rounded p-2"
              value={reminder}
              onChange={(event) => setReminder(event.target.value)}
            >
              {REMINDER_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="flex justify-end space-x-2 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded shadow transition"
            >
              Save Appointment
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
