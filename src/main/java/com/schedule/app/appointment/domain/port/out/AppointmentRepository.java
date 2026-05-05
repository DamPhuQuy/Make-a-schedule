package com.schedule.app.appointment.domain.port.out;

import com.schedule.app.appointment.domain.model.Appointment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(Long id);
    List<Appointment> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<Appointment> findGroupAppointment(String name, LocalDate date, Integer startHour, Integer endHour);
    Optional<Appointment> findUserAppointment(Long userId, LocalDate date, Integer startHour, Integer endHour);
    List<Appointment> findByUserId(Long userId);
}
