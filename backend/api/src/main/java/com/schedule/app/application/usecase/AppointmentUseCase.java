package com.schedule.app.application.usecase;

import java.util.List;

import com.schedule.app.application.dto.request.CreateAppointmentRequest;
import com.schedule.app.application.dto.response.AppointmentResponse;
import com.schedule.app.security.UserDetailsImpl;

public interface AppointmentUseCase {
    List<AppointmentResponse> getAllAppointments(UserDetailsImpl currentUser);
    AppointmentResponse createAppointment(CreateAppointmentRequest request, boolean forceReplace, boolean forceJoin, UserDetailsImpl currentUser);
    AppointmentResponse createGroupMeeting(CreateAppointmentRequest request, UserDetailsImpl currentUser);
}
