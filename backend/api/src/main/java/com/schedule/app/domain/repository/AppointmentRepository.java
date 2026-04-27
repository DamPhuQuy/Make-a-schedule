package com.schedule.app.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.schedule.app.infrastructure.persistence.entity.Appointment;

public interface AppointmentRepository {

    List<Appointment> findOverlappingAppointments(Long ownerId, LocalDateTime startTime, LocalDateTime endTime);

    List<Appointment> findByOwnerId(Long ownerId);

    List<Appointment> findByName(String name);

    Appointment save(Appointment appointment);

    void deleteAll(List<Appointment> appointments);
}
