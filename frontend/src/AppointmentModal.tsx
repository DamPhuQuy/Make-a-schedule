import React, { useState } from 'react';

export default function AppointmentModal({ slot, onClose, onSave }: any) {
  const [appointmentType, setAppointmentType] = useState("normal");
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [participantUsernames, setParticipantUsernames] = useState("");
  // Converting dates to local datetime-local format string
  const formatForInput = (d: Date) => {
    // offset to local timezone securely
    const offset = d.getTimezoneOffset() * 60000;
    return new Date(d.getTime() - offset).toISOString().slice(0, 16);
  };

  const [startTime, setStartTime] = useState(slot ? formatForInput(slot.start) : "");
  const [endTime, setEndTime] = useState(slot ? formatForInput(slot.end) : "");
  const [reminder, setReminder] = useState("0");
  const [error, setError] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError("Name cannot be empty.");
      return;
    }
    const start = new Date(startTime);
    const end = new Date(endTime);
    if (end <= start) {
      setError("End time must be securely after start time. Duration must be positive.");
      return;
    }

    onSave({
      appointmentType,
      name,
      location,
      participantUsernames: appointmentType === "group" ? participantUsernames : null,
      startTime: start.toISOString(),
      endTime: end.toISOString(),
      reminderMinutes: reminder !== "0" ? parseInt(reminder) : null
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-xl w-96 relative">
        <h2 className="text-2xl font-bold mb-4">Add Appointment</h2>
        {error && <div className="bg-red-100 text-red-700 p-2 rounded mb-4 text-sm">{error}</div>}
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Appointment Type</label>
            <select
              className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
              value={appointmentType}
              onChange={e => setAppointmentType(e.target.value)}
            >
              <option value="normal">Normal Appointment</option>
              <option value="group">Group Meeting</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              type="text"
              className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Doctor Appointment"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Location</label>
            <input
              type="text"
              className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
              value={location}
              onChange={e => setLocation(e.target.value)}
              placeholder="e.g. Office, Online"
            />
          </div>
          {appointmentType === "group" && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Participant Usernames</label>
              <input
                type="text"
                className="w-full border border-gray-300 rounded p-2 focus:ring-2 focus:ring-blue-500 outline-none transition"
                value={participantUsernames}
                onChange={e => setParticipantUsernames(e.target.value)}
                placeholder="e.g. user1, user2, user3"
              />
              <p className="text-xs text-gray-500 mt-1">Separate multiple usernames with commas</p>
            </div>
          )}
          <div className="flex space-x-2">
            <div className="w-1/2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
              <input 
                type="datetime-local" 
                className="w-full border border-gray-300 rounded p-2"
                value={startTime}
                onChange={e => setStartTime(e.target.value)}
              />
            </div>
            <div className="w-1/2">
              <label className="block text-sm font-medium text-gray-700 mb-1">End Time</label>
              <input 
                type="datetime-local" 
                className="w-full border border-gray-300 rounded p-2"
                value={endTime}
                onChange={e => setEndTime(e.target.value)}
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Reminder</label>
            <select 
              className="w-full border border-gray-300 rounded p-2"
              value={reminder}
              onChange={e => setReminder(e.target.value)}
            >
              <option value="0">None</option>
              <option value="15">15 minutes before</option>
              <option value="30">30 minutes before</option>
              <option value="60">1 hour before</option>
            </select>
          </div>
          <div className="flex justify-end space-x-2 pt-4">
            <button type="button" onClick={onClose} className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded transition">Cancel</button>
            <button type="submit" className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded shadow transition">Save Appointment</button>
          </div>
        </form>
      </div>
    </div>
  );
}
