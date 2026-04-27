package com.schedule.app.application.dto.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private boolean groupMeeting;

    private boolean forceReplace;
    private boolean forceJoin;

    @Size(min = 1, message = "Participants required for group meeting")
    private List<@NotBlank String> participantUsernames;

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) return true;
        return endTime.isAfter(startTime);
    }
}
