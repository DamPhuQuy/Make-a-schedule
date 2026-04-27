package com.schedule.app.application.dto.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAppointmentRequest {
    @NotBlank
    private String name;

    private String location;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    private Integer reminderMinutes;

    private boolean isGroupMeeting;

    private List<String> participantUsernames;
}
