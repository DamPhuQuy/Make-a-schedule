package com.schedule.app.application.service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.schedule.app.application.dto.request.LoginRequest;
import com.schedule.app.application.dto.response.LoginResponse;
import com.schedule.app.application.usecase.AuthenticationUseCase;
import com.schedule.app.domain.repository.RefreshTokenRepository;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.infrastructure.persistence.entity.RefreshToken;
import com.schedule.app.infrastructure.persistence.entity.User;
import com.schedule.app.security.UserDetailsImpl;
import com.schedule.app.security.jwt.JwtUseCase;

public class AuthenticationService implements AuthenticationUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUseCase jwtUtils;

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager,
            JwtUseCase jwtUtils) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public LoginResponse authenticate(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            String accessToken = jwtUtils.generateToken(userDetails);
            String refreshTokenString = jwtUtils.generateRefreshToken(userDetails);

            refreshTokenRepository.deleteByUser(user);

            RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(Instant.now().plusMillis(604800000))
                .revoked(false)
                .build();

            refreshTokenRepository.save(refreshToken);

            return new LoginResponse(accessToken, refreshTokenString);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

}
