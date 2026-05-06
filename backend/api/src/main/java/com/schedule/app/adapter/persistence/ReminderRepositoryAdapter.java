package com.schedule.app.adapter.persistence;

import java.util.List;

import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.infrastructure.persistence.entity.Reminder;
import com.schedule.app.infrastructure.persistence.jpa.ReminderJpaRepository;

public class ReminderRepositoryAdapter implements ReminderRepository {
    private final ReminderJpaRepository reminderJpaRepository;

    public ReminderRepositoryAdapter(ReminderJpaRepository reminderJpaRepository) {
        this.reminderJpaRepository = reminderJpaRepository;
    }

    @Override
    public List<Reminder> findByAppointmentId(Long appointmentId) {
        return reminderJpaRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public Reminder save(Reminder reminder) {
        return reminderJpaRepository.save(reminder);
    }

    @Override
    public void delete(Reminder reminder) {
        reminderJpaRepository.delete(reminder);
    }

    @Override
    public List<Reminder> findAllDueWithDynamicTime(java.time.Instant now) {
        return reminderJpaRepository.findAllDueWithDynamicTime(now);
    }
}
