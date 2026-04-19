package com.schedule.app.controller;

import com.schedule.app.dto.AppointmentDto;
import com.schedule.app.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*") // For local dev simplicity
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentDto> getAppointments() {
        return appointmentService.getAllAppointments();
    }

    @PostMapping
    public AppointmentDto createAppointment(@RequestBody AppointmentDto dto, 
                                            @RequestParam(required = false, defaultValue = "false") boolean forceReplace,
                                            @RequestParam(required = false, defaultValue = "false") boolean forceJoin) {
        try {
            return appointmentService.createAppointment(dto, forceReplace, forceJoin);
        } catch (ResponseStatusException e) {
            String reason = e.getReason();
            if (reason != null && reason.startsWith("OVERLAP:")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "\\\"" + reason + "\\\"");
            }
            if (reason != null && reason.startsWith("GROUP_MEETING:")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "\\\"" + reason + "\\\"");
            }
            throw e;
        }
    }
}
