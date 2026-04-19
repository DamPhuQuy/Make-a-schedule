package com.schedule.app.repository;

import com.schedule.app.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.owner.id = :ownerId AND (a.timeSlot.start_time < :endTime AND a.timeSlot.end_time > :startTime)")
    List<Appointment> findOverlappingAppointments(@Param("ownerId") Long ownerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Appointment> findByOwnerId(Long ownerId);
    
    List<Appointment> findByName(String name);
}
