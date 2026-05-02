package com.schedule.app.domain.repository;

import java.time.Instant;
import java.util.List;

import com.schedule.app.infrastructure.persistence.entity.Reminder;

public interface ReminderRepository {
    List<Reminder> findByAppointmentId(Long appointmentId);

    List<Reminder> findAllDueWithDynamicTime(Instant now);

    Reminder save(Reminder reminder);

    void delete(Reminder reminder);
}
