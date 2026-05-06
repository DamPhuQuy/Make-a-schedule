package com.schedule.app.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.schedule.app.infrastructure.persistence.entity.PersonalAppointment;

public interface PersonalAppointmentJpaRepository extends JpaRepository<PersonalAppointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE a.owner.id = :ownerId AND (a.timeSlot.startTime < :endTime AND a.timeSlot.endTime > :startTime)")
    List<PersonalAppointment> findOverlappingAppointments(@Param("ownerId") Long ownerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<PersonalAppointment> findByOwnerId(Long ownerId);

    List<PersonalAppointment> findByName(String name);
}
