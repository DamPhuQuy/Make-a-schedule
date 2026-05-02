package com.schedule.app.application.service;

import com.schedule.app.application.usecase.CreateReminderUseCase;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.infrastructure.persistence.entity.Reminder;

public class CreateReminderService implements CreateReminderUseCase {

    private final ReminderRepository reminderRepository;

    public CreateReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Override
    public Reminder execute(Long appointmentId, Integer minutesBefore) {
        Reminder reminder = Reminder.builder()
                .appointmentId(appointmentId)
                .minutesBefore(minutesBefore)
                .build();
        return reminderRepository.save(reminder);
    }
}
