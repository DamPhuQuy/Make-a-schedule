import { useEffect, useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import viLocale from '@fullcalendar/core/locales/vi';
import AppointmentModal from './AppointmentModal';
import ConflictWarning from './ConflictWarning';

export default function CalendarView({ onLogout }: { onLogout: () => void }) {
  const [events, setEvents] = useState<any[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<any>(null);
  const [conflictType, setConflictType] = useState<string | null>(null);
  const [conflictMessage, setConflictMessage] = useState("");
  const [pendingAppointment, setPendingAppointment] = useState<any>(null);
  const [culture, setCulture] = useState('en-US');

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const token = localStorage.getItem("jwt");
      const response = await fetch('http://localhost:8080/api/appointments', {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (response.status === 401) {
        onLogout();
        return;
      }
      const data = await response.json();
      const formattedData = data.map((appt: any) => ({
        ...appt,
        start: new Date(appt.startTime),
        end: new Date(appt.endTime),
        title: appt.name + (appt.groupMeeting ? " (Group)" : "")
      }));
      setEvents(formattedData);
    } catch (error) {
      console.error("Failed to fetch appointments", error);
    }
  };

  const handleSelectSlot = (selectInfo: any) => {
    setSelectedSlot({
      start: selectInfo.start,
      end: selectInfo.end
    });
    // Unselect the internal selection immediately so it doesn't linger visually after modal interactions
    selectInfo.view.calendar.unselect();
    setIsModalOpen(true);
  };

  const handleSaveAppointment = async (appointmentData: any, forceReplace = false, forceJoin = false) => {
    try {
      const token = localStorage.getItem("jwt");
      const query = new URLSearchParams({
        forceReplace: forceReplace.toString(),
        forceJoin: forceJoin.toString()
      });
      const response = await fetch(`http://localhost:8080/api/appointments?${query.toString()}`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(appointmentData)
      });

      if (response.status === 401) {
        onLogout();
        return;
      }

      if (response.status === 409) {
        const errorData = await response.json();
        const errorMessage = errorData.message || (errorData.trace && errorData.trace.includes("OVERLAP:") ? "OVERLAP" : "GROUP_MEETING");

        let type = errorMessage.includes("OVERLAP:") ? "OVERLAP" : errorMessage.includes("GROUP_MEETING:") ? "GROUP_MEETING" : "UNKNOWN";

        setConflictType(type);
        setConflictMessage(errorMessage);
        setPendingAppointment(appointmentData);
        // Keep modal open, show conflict warning on top
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
    } catch (error) {
      console.error("Failed to save appointment", error);
    }
  };

  return (
    <div className="p-4 bg-gray-50 min-h-screen">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">My Calendar</h1>
        <div className="flex space-x-2">
          <button 
            onClick={() => setCulture('en-US')}
            className={`px-3 py-1 rounded text-sm font-medium transition ${culture === 'en-US' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
          >
            English
          </button>
          <button 
            onClick={() => setCulture('vi')}
            className={`px-3 py-1 rounded text-sm font-medium transition ${culture === 'vi' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`}
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
      <div className="bg-white rounded-xl shadow p-4" style={{ height: '80vh' }}>
        <FullCalendar
          plugins={[timeGridPlugin, interactionPlugin]}
          initialView="timeGridWeek"
          headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: '' // Just view by week, no extra view buttons
          }}
          selectable={true}
          select={handleSelectSlot}
          events={events}
          locale={culture === 'vi' ? viLocale : undefined}
          height="100%"
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
          onCancel={() => setConflictType(null)}
          onReplace={() => handleSaveAppointment(pendingAppointment, true, false)}
          onJoin={() => handleSaveAppointment(pendingAppointment, false, true)}
        />
      )}
    </div>
  );
}
