package com.schedule.app.application.dto.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateAppointmentRequest {
    @NotBlank
    private String name;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;
}
