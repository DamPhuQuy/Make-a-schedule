package com.schedule.app.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.infrastructure.persistence.AppointmentJpaRepository;
import com.schedule.app.infrastructure.persistence.entity.Appointment;

public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository appointmentJpaRepository;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository appointmentJpaRepository) {
        this.appointmentJpaRepository = appointmentJpaRepository;
    }

    @Override
    public Appointment save(Appointment appointment) {
        return appointmentJpaRepository.save(appointment);
    }

    @Override
    public List<Appointment> findOverlappingAppointments(Long ownerId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentJpaRepository.findOverlappingAppointments(ownerId, startTime, endTime);
    }

    @Override
    public List<Appointment> findByOwnerId(Long ownerId) {
        return appointmentJpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Appointment> findByName(String name) {
        return appointmentJpaRepository.findByName(name);
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return appointmentJpaRepository.findById(id);
    }

    @Override
    public void deleteAll(List<Appointment> appointments) {
        appointmentJpaRepository.deleteAll(appointments);
    }

}
