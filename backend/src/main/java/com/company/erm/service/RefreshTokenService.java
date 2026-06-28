package com.company.erm.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.config.JwtProperties;
import com.company.erm.entity.RefreshToken;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.RefreshTokenRepository;

@Service
@Transactional
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public String issueRefreshToken(User user) {
        return createToken(user);
    }

    public record RotationResult(User user, String refreshToken) {}

    public RotationResult rotateRefreshToken(String rawToken) {
        RefreshToken existing = findActiveToken(rawToken);
        RefreshToken replacement = buildTokenEntity(existing.getUser());
        refreshTokenRepository.save(replacement);

        existing.setRevokedAt(Instant.now());
        existing.setReplacedById(replacement.getId());
        refreshTokenRepository.save(existing);

        return new RotationResult(existing.getUser(), replacement.getRawToken());
    }

    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash(rawToken))
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllActiveForUser(user.getId(), Instant.now());
    }

    private RefreshToken findActiveToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash(rawToken))
                .orElseThrow(() -> new BusinessException(
                        "AUTH_ERROR", "Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (!token.isActive()) {
            throw new BusinessException("AUTH_ERROR", "Refresh token expired", HttpStatus.UNAUTHORIZED);
        }
        return token;
    }

    private String createToken(User user) {
        RefreshToken entity = buildTokenEntity(user);
        refreshTokenRepository.save(entity);
        return entity.getRawToken();
    }

    private RefreshToken buildTokenEntity(User user) {
        String rawToken = generateRawToken();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()));
        entity.setRawToken(rawToken);
        return entity;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
