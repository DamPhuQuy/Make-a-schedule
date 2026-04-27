package com.schedule.app.application.service;

import com.schedule.app.application.usecase.LogoutUseCase;
import com.schedule.app.domain.repository.RefreshTokenRepository;

public class LogoutService implements LogoutUseCase {
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
