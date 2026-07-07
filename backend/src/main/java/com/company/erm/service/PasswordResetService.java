package com.company.erm.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.entity.PasswordResetToken;
import com.company.erm.entity.User;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.PasswordResetTokenRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long TOKEN_TTL_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Request a password reset. Always completes silently if the email is unknown (no enumeration).
     */
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email.trim())
            .filter(User::isActive)
            .ifPresent(this::issueResetToken);
    }

    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(hash(rawToken))
            .orElseThrow(() -> new BusinessException(
                "AUTH_ERROR", "Invalid or expired reset token", HttpStatus.BAD_REQUEST));

        if (!token.isActive()) {
            throw new BusinessException("AUTH_ERROR", "Invalid or expired reset token", HttpStatus.BAD_REQUEST);
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);
        refreshTokenService.revokeAllForUser(user);
    }

    private void issueResetToken(User user) {
        passwordResetTokenRepository.invalidateActiveTokensForUser(user.getId(), Instant.now());

        String rawToken = generateRawToken();
        PasswordResetToken entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plusSeconds(TOKEN_TTL_HOURS * 3600));
        passwordResetTokenRepository.save(entity);

        log.info("Password reset requested for userId={} at {}", user.getId(), Instant.now());
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
