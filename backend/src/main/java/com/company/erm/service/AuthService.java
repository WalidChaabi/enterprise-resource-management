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
import com.company.erm.security.MfaTokenProvider;
import com.company.erm.service.RefreshTokenService.RotationResult;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MfaService mfaService;
    private final MfaTokenProvider mfaTokenProvider;

    public AuthService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            RefreshTokenService refreshTokenService,
            MfaService mfaService,
            MfaTokenProvider mfaTokenProvider) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.mfaService = mfaService;
        this.mfaTokenProvider = mfaTokenProvider;
    }

    public record AuthSession(AuthResponse response, String accessToken, String refreshToken) {}

    public record LoginResult(boolean mfaRequired, String mfaToken, AuthSession session) {
        public static LoginResult pending(String mfaToken) {
            return new LoginResult(true, mfaToken, null);
        }

        public static LoginResult complete(AuthSession session) {
            return new LoginResult(false, null, session);
        }
    }

    @Transactional
    public LoginResult login(AuthRequest request) {
        User user = authenticate(request.email(), request.password());
        return beginAuthenticatedSession(user);
    }

    @Transactional
    public LoginResult verifyMfa(String mfaToken, String code) {
        UUID userId = parseMfaTokenUserId(mfaToken);
        mfaService.verifyLoginCode(userId, code);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("MFA_ERROR", "User not found", HttpStatus.UNAUTHORIZED));
        return LoginResult.complete(buildSession(user, user.getOrganization().getId()));
    }

    public LoginResult beginAuthenticatedSession(User user) {
        if (mfaService.isMfaRequired(user)) {
            UUID organizationId = user.getOrganization().getId();
            return LoginResult.pending(mfaTokenProvider.generateMfaPendingToken(user.getId(), organizationId));
        }
        return LoginResult.complete(buildSession(user, user.getOrganization().getId()));
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
        return buildSession(user, organizationId);
    }

    public AuthSession buildSessionAfterMfa(User user) {
        return buildSession(user, user.getOrganization().getId());
    }

    private UUID parseMfaTokenUserId(String mfaToken) {
        try {
            return mfaTokenProvider.getUserIdFromMfaToken(mfaToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException("MFA_ERROR", "MFA session expired", HttpStatus.UNAUTHORIZED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException("MFA_ERROR", "Invalid MFA token", HttpStatus.UNAUTHORIZED);
        }
    }

    private User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "AUTH_ERROR", "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new BusinessException("AUTH_ERROR", "User account is inactive", HttpStatus.UNAUTHORIZED);
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
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
