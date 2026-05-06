package com.schedule.app.application.usecase;

import com.schedule.app.application.dto.request.LoginRequest;
import com.schedule.app.application.dto.response.LoginResponse;

@FunctionalInterface
public interface AuthenticationUseCase {
    LoginResponse authenticate(LoginRequest request);
}
