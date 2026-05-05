package com.schedule.app.appointment.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "appointment")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "start_hour", nullable = false)
    private Integer startHour;

    @Column(name = "end_hour", nullable = false)
    private Integer endHour;

    @Column(name = "type_appointment", nullable = false)
    private String typeAppointment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "take",
        joinColumns = @JoinColumn(name = "appointment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<com.schedule.app.user.adapter.out.persistence.UserEntity> users = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "take_rmd",
        joinColumns = @JoinColumn(name = "appointment_id"),
        inverseJoinColumns = @JoinColumn(name = "reminder_id")
    )
    private Set<com.schedule.app.reminder.adapter.out.persistence.ReminderEntity> reminders = new HashSet<>();

    public AppointmentEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(LocalDate meetingDate) {
        this.meetingDate = meetingDate;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(Integer endHour) {
        this.endHour = endHour;
    }

    public String getTypeAppointment() {
        return typeAppointment;
    }

    public void setTypeAppointment(String typeAppointment) {
        this.typeAppointment = typeAppointment;
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

    public Set<com.schedule.app.user.adapter.out.persistence.UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<com.schedule.app.user.adapter.out.persistence.UserEntity> users) {
        this.users = users;
    }

    public Set<com.schedule.app.reminder.adapter.out.persistence.ReminderEntity> getReminders() {
        return reminders;
    }

    public void setReminders(Set<com.schedule.app.reminder.adapter.out.persistence.ReminderEntity> reminders) {
        this.reminders = reminders;
    }
}
