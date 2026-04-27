package com.schedule.app.domain.repository;

import java.util.Optional;

import com.schedule.app.infrastructure.persistence.entity.User;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
