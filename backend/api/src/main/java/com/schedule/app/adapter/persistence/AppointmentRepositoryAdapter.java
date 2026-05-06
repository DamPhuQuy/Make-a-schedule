package com.schedule.app.adapter.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.infrastructure.persistence.entity.Appointment;
import com.schedule.app.infrastructure.persistence.jpa.AppointmentJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository appointmentJpaRepository;

    @Override
    public List<Appointment> findOverlappingAppointments(Instant startTime, Instant endTime) {
        return appointmentJpaRepository.findOverlappingAppointments(startTime, endTime);
    }

    @Override
    public List<Appointment> findOverlappingAppointmentsForUser(Long userId, Instant startTime, Instant endTime) {
        return appointmentJpaRepository.findOverlappingAppointmentsForUser(userId, startTime, endTime);
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return appointmentJpaRepository.findById(id);
    }

    @Override
    public Appointment save(Appointment appointment) {
        return appointmentJpaRepository.save(appointment);
    }

    @Override
    public void delete(Appointment appointment) {
        appointmentJpaRepository.delete(appointment);
    }
}

