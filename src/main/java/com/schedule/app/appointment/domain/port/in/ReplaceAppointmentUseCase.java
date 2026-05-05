package com.schedule.app.appointment.domain.port.in;

import com.schedule.app.appointment.domain.model.Appointment;

public interface ReplaceAppointmentUseCase {
    Appointment replaceAppointment(Long oldAppointmentId, CreateAppointmentCommand newAppointmentCommand);
}
