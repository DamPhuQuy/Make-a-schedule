package com.schedule.app.application.usecase;

import java.util.List;

import com.schedule.app.application.dto.request.CreateAppointmentRequest;
import com.schedule.app.application.dto.request.ValidateAppointmentRequest;
import com.schedule.app.application.dto.response.AppointmentConflictResponse;
import com.schedule.app.application.dto.response.AppointmentResponse;
import com.schedule.app.security.UserDetailsImpl;

public interface AppointmentUseCase {
    List<AppointmentResponse> getAllAppointments(UserDetailsImpl currentUser);
    AppointmentConflictResponse validateAppointment(ValidateAppointmentRequest request, UserDetailsImpl currentUser);
    AppointmentResponse createAppointment(CreateAppointmentRequest request, UserDetailsImpl currentUser);
    AppointmentResponse createGroupMeeting(CreateAppointmentRequest request, UserDetailsImpl currentUser);
}
