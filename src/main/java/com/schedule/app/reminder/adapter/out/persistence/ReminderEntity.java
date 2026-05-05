package com.schedule.app.reminder.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reminder")
public class ReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "reminders")
    private Set<com.schedule.app.appointment.adapter.out.persistence.AppointmentEntity> appointments = new HashSet<>();

    public ReminderEntity() {
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

    public Set<com.schedule.app.appointment.adapter.out.persistence.AppointmentEntity> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<com.schedule.app.appointment.adapter.out.persistence.AppointmentEntity> appointments) {
        this.appointments = appointments;
    }
}
