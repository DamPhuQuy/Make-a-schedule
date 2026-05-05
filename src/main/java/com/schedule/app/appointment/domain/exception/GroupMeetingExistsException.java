package com.schedule.app.appointment.domain.exception;

import com.schedule.app.appointment.domain.model.Appointment;

public class GroupMeetingExistsException extends RuntimeException {
    private final Appointment existingGroupMeeting;

    public GroupMeetingExistsException(String message, Appointment existingGroupMeeting) {
        super(message);
        this.existingGroupMeeting = existingGroupMeeting;
    }

    public Appointment getExistingGroupMeeting() {
        return existingGroupMeeting;
    }
}
