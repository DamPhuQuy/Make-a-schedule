package com.schedule.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.schedule.app.application.service.AppointmentService;
import com.schedule.app.application.service.AuthenticationService;
import com.schedule.app.application.service.LogoutService;
import com.schedule.app.application.service.RegisterService;
import com.schedule.app.application.usecase.AppointmentUseCase;
import com.schedule.app.application.usecase.AuthenticationUseCase;
import com.schedule.app.application.usecase.LogoutUseCase;
import com.schedule.app.application.usecase.RegisterUseCase;
import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.domain.repository.GroupMeetingRepository;
import com.schedule.app.domain.repository.RefreshTokenRepository;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.security.UserDetailsServiceImpl;
import com.schedule.app.security.jwt.JwtProperties;
import com.schedule.app.security.jwt.JwtService;
import com.schedule.app.security.jwt.JwtUseCase;

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
    public AuthenticationUseCase authenticationUseCase(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager,
            JwtUseCase jwtUtils) {
        return new AuthenticationService(userRepository, refreshTokenRepository, authenticationManager, jwtUtils);
    }

    @Bean
    public RegisterUseCase registerUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return new RegisterService(userRepository, passwordEncoder);
    }

    @Bean
    public LogoutUseCase logoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        return new LogoutService(refreshTokenRepository);
    }

    @Bean
    public JwtUseCase jwtUseCase(JwtProperties jwtProperties) {
        return new JwtService(jwtProperties);
    }

    @Bean
    public UserDetailsServiceImpl userDetailsService(UserRepository userRepository) {
        return new UserDetailsServiceImpl(userRepository);
    }
}
