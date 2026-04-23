package com.schedule.app.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String name;
    private String location;
    private Instant startTime;
    private Instant endTime;
    private Integer reminderMinutes;
    private boolean isGroupMeeting;
    private String ownerUsername;
}
