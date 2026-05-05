package com.schedule.app.reminder.adapter.out.persistence;

import com.schedule.app.reminder.domain.model.Reminder;
import org.springframework.stereotype.Component;

@Component
public class ReminderMapper {

    public ReminderEntity toEntity(Reminder reminder) {
        ReminderEntity entity = new ReminderEntity();
        entity.setId(reminder.getId());
        entity.setTitle(reminder.getTitle());
        entity.setCreatedAt(reminder.getCreatedAt());
        entity.setUpdatedAt(reminder.getUpdatedAt());
        return entity;
    }

    public Reminder toDomain(ReminderEntity entity) {
        return new Reminder(
                entity.getId(),
                entity.getTitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
