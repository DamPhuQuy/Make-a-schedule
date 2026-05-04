package com.schedule.app.application.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentConflictResponse {
    private ConflictType conflictType;
    private String message;
    private ConflictDetails details;

    public enum ConflictType {
        TIME_OVERLAP,
        GROUP_MEETING_MATCH,
        NONE
    }

    @Data
    @Builder
    public static class ConflictDetails {
        private Long conflictingAppointmentId;
        private String conflictingAppointmentName;
        private Instant conflictingStartTime;
        private Instant conflictingEndTime;

        private Long matchingGroupMeetingId;
        private String matchingGroupMeetingName;
        private Instant matchingGroupStartTime;
        private Instant matchingGroupEndTime;
        private List<String> participants;
    }
}
