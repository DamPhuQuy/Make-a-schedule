package com.schedule.app.appointment.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.schedule.app.appointment.domain.exception.AppointmentConflictException;
import com.schedule.app.appointment.domain.exception.GroupMeetingExistsException;

@RestControllerAdvice
public class AppointmentExceptionHandler {

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ConflictResponse> handleAppointmentConflict(AppointmentConflictException ex) {
        ConflictResponse response = new ConflictResponse(
            "APPOINTMENT_CONFLICT",
            ex.getMessage(),
            AppointmentResponse.fromDomain(ex.getConflictingAppointment())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(GroupMeetingExistsException.class)
    public ResponseEntity<GroupMeetingConflictResponse> handleGroupMeetingExists(GroupMeetingExistsException ex) {
        GroupMeetingConflictResponse response = new GroupMeetingConflictResponse(
            "GROUP_MEETING_EXISTS",
            ex.getMessage(),
            AppointmentResponse.fromDomain(ex.getExistingGroupMeeting())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    public static class ConflictResponse {
        private String errorCode;
        private String message;
        private AppointmentResponse conflictingAppointment;

        public ConflictResponse(String errorCode, String message, AppointmentResponse conflictingAppointment) {
            this.errorCode = errorCode;
            this.message = message;
            this.conflictingAppointment = conflictingAppointment;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public AppointmentResponse getConflictingAppointment() {
            return conflictingAppointment;
        }

        public void setConflictingAppointment(AppointmentResponse conflictingAppointment) {
            this.conflictingAppointment = conflictingAppointment;
        }
    }

    public static class GroupMeetingConflictResponse {
        private String errorCode;
        private String message;
        private AppointmentResponse existingGroupMeeting;

        public GroupMeetingConflictResponse(String errorCode, String message, AppointmentResponse existingGroupMeeting) {
            this.errorCode = errorCode;
            this.message = message;
            this.existingGroupMeeting = existingGroupMeeting;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public AppointmentResponse getExistingGroupMeeting() {
            return existingGroupMeeting;
        }

        public void setExistingGroupMeeting(AppointmentResponse existingGroupMeeting) {
            this.existingGroupMeeting = existingGroupMeeting;
        }
    }
}
