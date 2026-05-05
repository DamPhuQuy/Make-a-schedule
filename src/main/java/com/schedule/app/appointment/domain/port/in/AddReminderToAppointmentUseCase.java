package com.schedule.app.appointment.domain.port.in;

public interface AddReminderToAppointmentUseCase {
    void addReminderToAppointment(Long appointmentId, Long reminderId);
}
