package com.company.erm.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.AuthRequest;
import com.company.erm.dto.AuthResponse;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.JwtTokenProvider;
import com.company.erm.service.RefreshTokenService.RotationResult;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public record AuthSession(AuthResponse response, String accessToken, String refreshToken) {}

    @Transactional(readOnly = true)
    public AuthSession login(AuthRequest request) {
        User user = authenticate(request.email(), request.password());
        return buildSession(user, user.getOrganization().getId());
    }

    @Transactional
    public AuthSession refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException("AUTH_ERROR", "Refresh token missing", HttpStatus.UNAUTHORIZED);
        }
        RotationResult rotation = refreshTokenService.rotateRefreshToken(rawRefreshToken);
        User user = rotation.user();
        return buildSession(user, user.getOrganization().getId(), rotation.refreshToken());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRefreshToken(rawRefreshToken);
    }

    public AuthSession buildSessionForOrganization(User user, UUID organizationId) {
        return buildSessionWithTokens(user, organizationId, null);
    }

    private User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "AUTH_ERROR", "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new BusinessException("AUTH_ERROR", "User account is inactive", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("AUTH_ERROR", "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    private AuthSession buildSession(User user, UUID organizationId) {
        String refreshToken = refreshTokenService.issueRefreshToken(user);
        return buildSessionWithTokens(user, organizationId, refreshToken);
    }

    private AuthSession buildSession(User user, UUID organizationId, String refreshToken) {
        return buildSessionWithTokens(user, organizationId, refreshToken);
    }

    private AuthSession buildSessionWithTokens(User user, UUID organizationId, String refreshToken) {
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        List<String> roleCodes = userRoles.stream()
                .map(ur -> ur.getRole().getCode())
                .toList();

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), organizationId);

        AuthResponse response = new AuthResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                organizationId,
                roleCodes
        );

        return new AuthSession(response, accessToken, refreshToken);
    }
}
