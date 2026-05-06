package com.schedule.app.security.jwt;

import com.schedule.app.security.UserDetailsImpl;

public interface JwtUseCase {
    String generateToken(UserDetailsImpl userDetails);

    String generateRefreshToken(UserDetailsImpl userDetails);

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetailsImpl userDetails);

    long getAccessExpirationMs();

    long getRefreshExpirationMs();
}

