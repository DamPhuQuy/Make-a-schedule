package com.schedule.app.reminder.adapter.in.web;

import com.schedule.app.reminder.domain.model.Reminder;
import java.time.LocalDateTime;

public class ReminderResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReminderResponse() {
    }

    public ReminderResponse(Long id, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReminderResponse fromDomain(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
