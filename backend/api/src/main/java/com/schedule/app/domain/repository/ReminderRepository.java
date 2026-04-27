package com.schedule.app.domain.repository;

import java.util.List;

import com.schedule.app.infrastructure.persistence.entity.Reminder;

public interface ReminderRepository {
    List<Reminder> findByAppointmentId(Long appointmentId);

    Reminder save(Reminder reminder);
}
