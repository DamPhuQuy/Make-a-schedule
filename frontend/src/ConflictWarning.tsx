export type ConflictType = "OVERLAP" | "GROUP_MEETING";

export type ConflictDetails = {
  conflictingAppointmentName?: string;
  conflictingStartTime?: string;
  conflictingEndTime?: string;
  matchingGroupMeetingName?: string;
  matchingGroupStartTime?: string;
  matchingGroupEndTime?: string;
  participants?: string[];
};

type ConflictWarningProps = Readonly<{
  type: ConflictType;
  message: string;
  details?: ConflictDetails | null;
  onCancel: () => void;
  onReplace: () => void;
  onJoin: () => void;
}>;

export default function ConflictWarning({
  type,
  message,
  details,
  onCancel,
  onReplace,
  onJoin,
}: ConflictWarningProps) {
  const isOverlap = type === "OVERLAP";
  const showOverlapDetails =
    isOverlap && Boolean(details?.conflictingAppointmentName);
  const showGroupDetails =
    !isOverlap && Boolean(details?.matchingGroupMeetingName);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex justify-center items-center z-60">
      <div className="bg-white p-6 rounded-lg shadow-2xl w-96 transform transition-all scale-100 border-l-4 border-yellow-500">
        <h3 className="text-xl font-bold mb-2 flex items-center text-yellow-600">
          <svg
            className="w-6 h-6 mr-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            ></path>
          </svg>
          Conflict Detected
        </h3>
        <p className="text-gray-700 mb-4 font-semibold">{message}</p>

        {showOverlapDetails && (
          <div className="bg-gray-50 p-3 rounded mb-4 text-sm">
            <p className="font-medium text-gray-800">
              Conflicting Appointment:
            </p>
            <p className="text-gray-600">
              {details?.conflictingAppointmentName}
            </p>
            <p className="text-gray-500 text-xs mt-1">
              {details?.conflictingStartTime
                ? new Date(details.conflictingStartTime).toLocaleString()
                : ""}{" "}
              -
              {details?.conflictingEndTime
                ? new Date(details.conflictingEndTime).toLocaleTimeString()
                : ""}
            </p>
          </div>
        )}

        {showGroupDetails && (
          <div className="bg-gray-50 p-3 rounded mb-4 text-sm">
            <p className="font-medium text-gray-800">Group Meeting:</p>
            <p className="text-gray-600">{details?.matchingGroupMeetingName}</p>
            <p className="text-gray-500 text-xs mt-1">
              {details?.matchingGroupStartTime
                ? new Date(details.matchingGroupStartTime).toLocaleString()
                : ""}{" "}
              -
              {details?.matchingGroupEndTime
                ? new Date(details.matchingGroupEndTime).toLocaleTimeString()
                : ""}
            </p>
            {details?.participants && details.participants.length > 0 && (
              <p className="text-gray-500 text-xs mt-2">
                Participants: {details.participants.join(", ")}
              </p>
            )}
          </div>
        )}

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
