package com.schedule.app.appointment.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.schedule.app.appointment.domain.model.Appointment;
import com.schedule.app.appointment.domain.port.out.AppointmentRepository;

@Component
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpaRepository;
    private final AppointmentMapper mapper;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository jpaRepository, AppointmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentEntity entity = mapper.toEntity(appointment);
        AppointmentEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<Appointment> findGroupAppointment(String name, LocalDate date, Integer startHour, Integer endHour) {
        return jpaRepository.findGroupAppointment(name, date, startHour, endHour)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Appointment> findUserAppointment(Long userId, LocalDate date, Integer startHour, Integer endHour) {
        return jpaRepository.findUserAppointment(userId, date, startHour, endHour)
                .map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
