package com.schedule.app.adapter.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.app.application.dto.request.CreateAppointmentRequest;
import com.schedule.app.application.dto.response.AppointmentResponse;
import com.schedule.app.application.service.AppointmentService;
import com.schedule.app.security.UserDetailsImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentResponse> getAppointments(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return appointmentService.getAllAppointments(currentUser);
    }

    @PostMapping
    public AppointmentResponse createAppointment(@Valid @RequestBody CreateAppointmentRequest request,
                                            @RequestParam(required = false, defaultValue = "false") boolean forceReplace,
                                            @RequestParam(required = false, defaultValue = "false") boolean forceJoin,
                                            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        try {
            return appointmentService.createAppointment(request, forceReplace, forceJoin, currentUser);
        } catch (ResponseStatusException e) {
            String reason = e.getReason();
            if (reason != null && reason.startsWith("OVERLAP:")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "\"" + reason + "\"");
            }
            if (reason != null && reason.startsWith("GROUP_MEETING:")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "\"" + reason + "\"");
            }
            throw e;
        }
    }

    @PostMapping("/group")
    public AppointmentResponse createGroupMeeting(@Valid @RequestBody CreateAppointmentRequest request,
                                                   @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return appointmentService.createGroupMeeting(request, currentUser);
    }
}
