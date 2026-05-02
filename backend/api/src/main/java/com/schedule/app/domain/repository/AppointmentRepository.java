package com.schedule.app.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.schedule.app.infrastructure.persistence.entity.Appointment;

public interface AppointmentRepository {

    List<Appointment> findOverlappingAppointments(Long ownerId, LocalDateTime startTime, LocalDateTime endTime);

    List<Appointment> findByOwnerId(Long ownerId);

    List<Appointment> findByName(String name);

    Optional<Appointment> findById(Long id);

    Appointment save(Appointment appointment);

    void deleteAll(List<Appointment> appointments);
}
