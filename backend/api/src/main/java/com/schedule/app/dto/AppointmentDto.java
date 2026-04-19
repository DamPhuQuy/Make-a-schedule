package com.schedule.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

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
