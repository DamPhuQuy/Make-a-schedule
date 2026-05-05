package com.schedule.app.appointment.domain.port.in;

import com.schedule.app.appointment.domain.model.Appointment;
import java.util.List;

public interface GetAppointmentUseCase {
    Appointment getAppointmentById(Long id);
    List<Appointment> getAllAppointments();
    List<Appointment> getAppointmentsByUserId(Long userId);
}
