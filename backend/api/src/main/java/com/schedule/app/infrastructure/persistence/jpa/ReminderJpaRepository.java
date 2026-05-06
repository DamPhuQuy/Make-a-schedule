package com.schedule.app.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.schedule.app.infrastructure.persistence.entity.Reminder;

public interface ReminderJpaRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByAppointmentId(Long appointmentId);


    @Query(value = "SELECT r.* FROM reminders r " +
           "JOIN appointments a ON r.appointment_id = a.id " +
           "WHERE a.start_time - (r.minutes_before * INTERVAL '1 minute') <= :now " +
           "AND a.start_time > :now",
           nativeQuery = true)
    List<Reminder> findAllDueWithDynamicTime(@Param("now") Instant now);
}
