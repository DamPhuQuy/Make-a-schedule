package com.schedule.app.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.schedule.app.infrastructure.persistence.entity.Appointment;

public interface AppointmentJpaRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE a.timeSlot.startTime < :endTime AND a.timeSlot.endTime > :startTime")
    List<Appointment> findOverlappingAppointments(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN PersonalAppointment pa ON a.id = pa.id " +
           "LEFT JOIN GroupMeeting gm ON a.id = gm.id " +
           "LEFT JOIN gm.participants p " +
           "WHERE (pa.owner.id = :userId OR p.user.id = :userId) " +
           "AND a.timeSlot.startTime < :endTime AND a.timeSlot.endTime > :startTime")
    List<Appointment> findOverlappingAppointmentsForUser(@Param("userId") Long userId,
                                                    @Param("startTime") Instant startTime,
                                                    @Param("endTime") Instant endTime);
}
