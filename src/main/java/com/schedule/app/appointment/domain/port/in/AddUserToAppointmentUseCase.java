package com.schedule.app.appointment.domain.port.in;

public interface AddUserToAppointmentUseCase {
    void addUserToAppointment(Long userId, Long appointmentId);
}
