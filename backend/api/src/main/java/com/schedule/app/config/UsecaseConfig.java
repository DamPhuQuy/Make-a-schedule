package com.schedule.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import com.schedule.app.application.service.AppointmentService;
import com.schedule.app.application.service.AuthenticationService;
import com.schedule.app.application.usecase.AppointmentUseCase;
import com.schedule.app.application.usecase.AuthenticationUseCase;
import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.domain.repository.GroupMeetingRepository;
import com.schedule.app.domain.repository.RefreshTokenRepository;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.security.JwtUtils;

@Configuration
public class UsecaseConfig {
    @Bean
    public AppointmentUseCase appointmentUseCase(
            AppointmentRepository appointmentRepository,
            GroupMeetingRepository groupMeetingRepository,
            ReminderRepository reminderRepository,
            UserRepository userRepository) {
        return new AppointmentService(appointmentRepository, groupMeetingRepository, reminderRepository, userRepository);
    }

    @Bean
    public AuthenticationService authenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        return new AuthenticationService(userRepository, refreshTokenRepository, authenticationManager, jwtUtils);
    }
}
