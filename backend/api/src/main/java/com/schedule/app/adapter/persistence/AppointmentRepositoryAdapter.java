package com.schedule.app.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.infrastructure.persistence.PersonalAppointmentJpaRepository;
import com.schedule.app.infrastructure.persistence.entity.PersonalAppointment;

public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final PersonalAppointmentJpaRepository appointmentJpaRepository;

    public AppointmentRepositoryAdapter(PersonalAppointmentJpaRepository appointmentJpaRepository) {
        this.appointmentJpaRepository = appointmentJpaRepository;
    }

    @Override
    public PersonalAppointment save(PersonalAppointment appointment) {
        return appointmentJpaRepository.save(appointment);
    }

    @Override
    public List<PersonalAppointment> findOverlappingAppointments(Long ownerId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentJpaRepository.findOverlappingAppointments(ownerId, startTime, endTime);
    }

    @Override
    public List<PersonalAppointment> findByOwnerId(Long ownerId) {
        return appointmentJpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<PersonalAppointment> findByName(String name) {
        return appointmentJpaRepository.findByName(name);
    }

    @Override
    public Optional<PersonalAppointment> findById(Long id) {
        return appointmentJpaRepository.findById(id);
    }

    @Override
    public void delete(PersonalAppointment appointment) {
        appointmentJpaRepository.delete(appointment);
    }

    @Override
    public void deleteAll(List<PersonalAppointment> appointments) {
        appointmentJpaRepository.deleteAll(appointments);
    }

}
