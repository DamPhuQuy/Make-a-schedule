package com.schedule.app.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.schedule.app.infrastructure.persistence.entity.Appointment;

public interface AppointmentRepository {

    List<Appointment> findOverlappingAppointments(Instant startTime, Instant endTime);

    List<Appointment> findOverlappingAppointmentsForUser(Long userId, Instant startTime, Instant endTime);

    Optional<Appointment> findById(Long id);

    Appointment save(Appointment base);

    void delete(Appointment base);
}
