package com.schedule.app.adapter.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schedule.app.application.dto.request.LoginRequest;
import com.schedule.app.application.dto.request.RegisterRequest;
import com.schedule.app.application.dto.response.LoginResponse;
import com.schedule.app.application.dto.response.MessageResponse;
import com.schedule.app.application.usecase.AuthenticationUseCase;
import com.schedule.app.application.usecase.LogoutUseCase;
import com.schedule.app.application.usecase.RegisterUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationUseCase authenticationUseCase;
    private final RegisterUseCase registerUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(AuthenticationUseCase authenticationUseCase, RegisterUseCase registerUseCase, LogoutUseCase logoutUseCase) {
        this.authenticationUseCase = authenticationUseCase;
        this.registerUseCase = registerUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationUseCase.authenticate(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody String refreshToken) {
        logoutUseCase.logout(refreshToken);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        return ResponseEntity.ok(registerUseCase.register(signUpRequest));
    }
}

