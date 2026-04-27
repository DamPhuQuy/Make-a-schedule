package com.schedule.app.application.usecase;

import com.schedule.app.application.dto.request.LogoutRequest;

@FunctionalInterface
public interface LogoutUseCase {
    void logout(LogoutRequest request);
}
