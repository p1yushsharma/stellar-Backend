package com.demo.auth.service;

import com.demo.auth.util.JwtUtil;
import com.demo.auth.entity.RefreshToken;
import com.demo.auth.entity.User;
import com.demo.auth.repository.RefreshTokenRepository;
import com.demo.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        refreshTokenRepository.save(refreshToken);
        refreshToken.setToken(rawToken); 

        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String rawToken) {
        return refreshTokenRepository.findAll().stream()
                .filter(storedToken -> passwordEncoder.matches(rawToken, storedToken.getToken()))
                .findFirst();
    }

    public boolean verifyExpiration(RefreshToken token) {
        return token.getExpiryDate().isAfter(Instant.now());
    }

    public String refreshAccessToken(String rawToken) {
        RefreshToken tokenFromDb = findByToken(rawToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!verifyExpiration(tokenFromDb)) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = tokenFromDb.getUser();
        return jwtUtil.generateToken(user.getEmail());
    }
    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }
}
