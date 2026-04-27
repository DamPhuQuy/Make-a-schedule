package com.schedule.app.application.usecase;

@FunctionalInterface
public interface LogoutUseCase {
    void logout(String refreshToken);
}
