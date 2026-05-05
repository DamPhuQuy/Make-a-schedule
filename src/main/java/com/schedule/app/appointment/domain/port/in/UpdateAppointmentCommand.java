package com.schedule.app.appointment.domain.port.in;

import java.time.LocalDate;

public class UpdateAppointmentCommand {
    private String name;
    private String location;
    private LocalDate meetingDate;
    private Integer startHour;
    private Integer endHour;
    private String typeAppointment;

    public UpdateAppointmentCommand() {
    }

    public UpdateAppointmentCommand(String name, String location, LocalDate meetingDate,
                                   Integer startHour, Integer endHour, String typeAppointment) {
        this.name = name;
        this.location = location;
        this.meetingDate = meetingDate;
        this.startHour = startHour;
        this.endHour = endHour;
        this.typeAppointment = typeAppointment;
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
}
