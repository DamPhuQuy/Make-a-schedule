package com.schedule.app.appointment.domain.port.in;

public interface JoinGroupMeetingUseCase {
    void joinGroupMeeting(Long userId, Long groupMeetingId);
}
