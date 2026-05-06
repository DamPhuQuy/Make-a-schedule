package com.schedule.app.application.usecase;

import com.schedule.app.infrastructure.persistence.entity.Reminder;

public interface CreateReminderUseCase {
    Reminder execute(Long appointmentId, Integer minutesBefore);
}
