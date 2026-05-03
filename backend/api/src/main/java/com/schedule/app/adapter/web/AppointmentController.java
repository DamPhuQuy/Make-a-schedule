package com.schedule.app.adapter.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schedule.app.application.dto.request.CreateAppointmentRequest;
import com.schedule.app.application.dto.request.ValidateAppointmentRequest;
import com.schedule.app.application.dto.response.AppointmentConflictResponse;
import com.schedule.app.application.dto.response.AppointmentResponse;
import com.schedule.app.application.usecase.AppointmentUseCase;
import com.schedule.app.security.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentUseCase appointmentUseCase;

    public AppointmentController(AppointmentUseCase appointmentUseCase) {
        this.appointmentUseCase = appointmentUseCase;
    }

    @GetMapping
    public List<AppointmentResponse> getAppointments(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return appointmentUseCase.getAllAppointments(currentUser);
    }

    @PostMapping("/validate")
    public AppointmentConflictResponse validateAppointment(@Valid @RequestBody ValidateAppointmentRequest request,
                                                           @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return appointmentUseCase.validateAppointment(request, currentUser);
    }

    @PostMapping
    public AppointmentResponse createAppointment(@Valid @RequestBody CreateAppointmentRequest request,
                                            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return appointmentUseCase.createAppointment(request, currentUser);
    }

    @PostMapping("/group")
    public AppointmentResponse createGroupMeeting(@Valid @RequestBody CreateAppointmentRequest request,
                                                   @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return appointmentUseCase.createGroupMeeting(request, currentUser);
    }
}
