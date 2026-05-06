package com.schedule.app.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.schedule.app.infrastructure.persistence.entity.PersonalAppointment;

public interface PersonalAppointmentRepository {

    List<PersonalAppointment> findOverlappingAppointments(Long ownerId, Instant startTime, Instant endTime);

    List<PersonalAppointment> findByOwnerId(Long ownerId);

    List<PersonalAppointment> findByName(String name);

    Optional<PersonalAppointment> findById(Long id);

    PersonalAppointment save(PersonalAppointment appointment);

    void delete(PersonalAppointment appointment);

    void deleteAll(List<PersonalAppointment> appointments);
}
