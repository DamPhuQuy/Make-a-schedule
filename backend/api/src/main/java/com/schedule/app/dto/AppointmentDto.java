package com.schedule.app.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AppointmentDto {
    private Long id;
    private String name;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer reminderMinutes; // optional reminder
    private boolean isGroupMeeting;
}
