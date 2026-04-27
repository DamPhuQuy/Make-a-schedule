package com.schedule.app.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedule.app.infrastructure.persistence.entity.Reminder;

public interface ReminderJpaRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByAppointmentId(Long appointmentId);
}
