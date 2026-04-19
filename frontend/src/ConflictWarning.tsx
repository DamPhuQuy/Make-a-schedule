
export default function ConflictWarning({ type, message, onCancel, onReplace, onJoin }: any) {
  const isOverlap = type === 'OVERLAP';

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex justify-center items-center z-[60]">
      <div className="bg-white p-6 rounded-lg shadow-2xl w-96 transform transition-all scale-100 border-l-4 border-yellow-500">
        <h3 className="text-xl font-bold mb-2 flex items-center text-yellow-600">
          <svg className="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
          Conflict Detected
        </h3>
        <p className="text-gray-700 mb-6 font-semibold">
           {message}
        </p>
        <p className="text-gray-600 mb-6">
          {isOverlap
            ? "You already have an appointment at this time. Please choose an available time or replace the previous appointment."
            : "An appointment with this name and duration exists. Did you intend to join that group meeting instead?"}
        </p>

        <div className="flex flex-col space-y-2">
          {isOverlap ? (
            <button
              onClick={onReplace}
              className="w-full px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded font-medium transition"
            >
              Replace Existing Appointment
            </button>
          ) : (
            <button
              onClick={onJoin}
              className="w-full px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded font-medium transition"
            >
              Yes, Join Group Meeting
            </button>
          )}
          <button
            onClick={onCancel}
            className="w-full px-4 py-2 border border-gray-300 hover:bg-gray-50 text-gray-700 rounded font-medium transition"
          >
            Cancel / Choose Another Time
          </button>
        </div>
      </div>
    </div>
  );
}
