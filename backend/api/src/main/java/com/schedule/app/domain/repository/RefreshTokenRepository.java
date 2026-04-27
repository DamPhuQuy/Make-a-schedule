package com.schedule.app.domain.repository;

import java.util.Optional;

import com.schedule.app.infrastructure.persistence.entity.RefreshToken;
import com.schedule.app.infrastructure.persistence.entity.User;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
}
