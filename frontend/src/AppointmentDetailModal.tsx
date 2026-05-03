export default function AppointmentDetailModal({ appointment, onClose }: { appointment: any, onClose: () => void }) {
  // Hàm format thời gian hiển thị cho đẹp
  const formatDate = (date: Date) => {
    if (!date) return "";
    return new Intl.DateTimeFormat('vi-VN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };



  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
        <div className="flex justify-between items-center mb-4 border-b pb-2">
          <h2 className="text-xl font-bold text-gray-800">Chi tiết cuộc hẹn</h2>
          <button 
            onClick={onClose}
            className="text-gray-500 hover:text-red-500 font-bold text-xl"
          >
            &times;
          </button>
        </div>

        <div className="space-y-3 text-gray-700">
          <div>
            <span className="font-semibold">Tiêu đề: </span>
            <span>{appointment.title || appointment.name}</span>
          </div>

          <div>
            <span className="font-semibold">Bắt đầu: </span>
            <span>{formatDate(appointment.start)}</span>
          </div>

          <div>
            <span className="font-semibold">Kết thúc: </span>
            <span>{formatDate(appointment.end)}</span>
          </div>

          {appointment.location && (
            <div>
              <span className="font-semibold">Địa điểm: </span>
              <span>{appointment.location}</span>
            </div>
          )}

          {appointment.reminderMinutes !== undefined && (
            <div>
              <span className="font-semibold">Nhắc nhở trước: </span>
              <span>{appointment.ReminderMinutes || appointment.reminderMinutes} phút</span>
            </div>
          )}
        </div>

        <div className="mt-6 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}