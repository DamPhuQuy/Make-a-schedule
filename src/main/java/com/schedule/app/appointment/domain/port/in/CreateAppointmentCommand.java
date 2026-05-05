package com.schedule.app.appointment.domain.port.in;

import java.time.LocalDate;

import com.schedule.app.appointment.domain.enums.TypeAppointment;

public class CreateAppointmentCommand {
    private Long userId;
    private String name;
    private String location;
    private LocalDate meetingDate;
    private Integer startHour;
    private Integer endHour;
    private TypeAppointment typeAppointment;

    public CreateAppointmentCommand() {
    }

    public CreateAppointmentCommand(Long userId, String name, String location, LocalDate meetingDate,
                                   Integer startHour, Integer endHour, TypeAppointment typeAppointment) {
        this.userId = userId;
        this.name = name;
        this.location = location;
        this.meetingDate = meetingDate;
        this.startHour = startHour;
        this.endHour = endHour;
        this.typeAppointment = typeAppointment;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public TypeAppointment getTypeAppointment() {
        return typeAppointment;
    }

    public void setTypeAppointment(TypeAppointment typeAppointment) {
        this.typeAppointment = typeAppointment;
    }
}
