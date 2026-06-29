package com.company.erm.controller;

import com.company.erm.dto.AuthRequest;
import com.company.erm.dto.AuthResponse;
import com.company.erm.dto.ForgotPasswordRequestDTO;
import com.company.erm.dto.ResetPasswordRequestDTO;
import com.company.erm.security.AuthCookieService;
import com.company.erm.service.AuthService;
import com.company.erm.service.AuthService.AuthSession;
import com.company.erm.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthService authService,
            AuthCookieService authCookieService,
            PasswordResetService passwordResetService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        AuthSession session = authService.login(request);
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.ok(session.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.readRefreshToken(request);
        AuthSession session = authService.refresh(refreshToken);
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(authCookieService.readRefreshToken(request));
        authCookieService.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
