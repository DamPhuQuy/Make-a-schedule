package com.schedule.app.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedule.app.infrastructure.persistence.entity.RefreshToken;
import com.schedule.app.infrastructure.persistence.entity.User;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
}
