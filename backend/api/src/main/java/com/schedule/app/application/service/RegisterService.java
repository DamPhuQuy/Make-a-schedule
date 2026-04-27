package com.schedule.app.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.schedule.app.application.dto.request.RegisterRequest;
import com.schedule.app.application.dto.response.MessageResponse;
import com.schedule.app.application.usecase.RegisterUseCase;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.infrastructure.persistence.entity.User;

public class RegisterService implements RegisterUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }
}
