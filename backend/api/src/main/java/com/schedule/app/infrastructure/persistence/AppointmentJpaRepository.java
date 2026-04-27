package com.schedule.app.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.schedule.app.infrastructure.persistence.entity.Appointment;

public interface AppointmentJpaRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE a.owner.id = :ownerId AND (a.timeSlot.start_time < :endTime AND a.timeSlot.end_time > :startTime)")
    List<Appointment> findOverlappingAppointments(@Param("ownerId") Long ownerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Appointment> findByOwnerId(Long ownerId);

    List<Appointment> findByName(String name);
}
