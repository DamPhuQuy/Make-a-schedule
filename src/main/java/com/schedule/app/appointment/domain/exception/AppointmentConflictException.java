package com.schedule.app.appointment.domain.exception;

import com.schedule.app.appointment.domain.model.Appointment;

public class AppointmentConflictException extends RuntimeException {
    private final Appointment conflictingAppointment;

    public AppointmentConflictException(String message, Appointment conflictingAppointment) {
        super(message);
        this.conflictingAppointment = conflictingAppointment;
    }

    public Appointment getConflictingAppointment() {
        return conflictingAppointment;
    }
}
