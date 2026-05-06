package com.schedule.app.adapter.persistence;

import java.util.Optional;

import com.schedule.app.domain.repository.RefreshTokenRepository;
import com.schedule.app.infrastructure.persistence.RefreshTokenJpaRepository;
import com.schedule.app.infrastructure.persistence.entity.RefreshToken;
import com.schedule.app.infrastructure.persistence.entity.User;

public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenJpaRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token);
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenJpaRepository.deleteByUser(user);
    }

    @Override
    public void deleteByToken(String token) {
        refreshTokenJpaRepository.deleteByToken(token);
    }
}
