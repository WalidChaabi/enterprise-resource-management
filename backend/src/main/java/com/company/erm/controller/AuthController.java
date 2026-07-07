package com.company.erm.controller;

import com.company.erm.dto.AuthRequest;
import com.company.erm.dto.AuthResponse;
import com.company.erm.dto.ForgotPasswordRequestDTO;
import com.company.erm.dto.LoginResponseDTO;
import com.company.erm.dto.MfaVerifyRequestDTO;
import com.company.erm.dto.ResetPasswordRequestDTO;
import com.company.erm.dto.SignupRequestDTO;
import com.company.erm.dto.SsoProvidersResponseDTO;
import com.company.erm.security.AuthCookieService;
import com.company.erm.service.AuthService;
import com.company.erm.service.AuthService.AuthSession;
import com.company.erm.service.AuthService.LoginResult;
import com.company.erm.service.OrganizationProvisioningService;
import com.company.erm.service.PasswordResetService;
import com.company.erm.service.SsoAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "Login, signup, MFA, SSO, and session management")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final PasswordResetService passwordResetService;
    private final OrganizationProvisioningService organizationProvisioningService;
    private final SsoAuthService ssoAuthService;

    public AuthController(
            AuthService authService,
            AuthCookieService authCookieService,
            PasswordResetService passwordResetService,
            OrganizationProvisioningService organizationProvisioningService,
            SsoAuthService ssoAuthService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.passwordResetService = passwordResetService;
        this.organizationProvisioningService = organizationProvisioningService;
        this.ssoAuthService = ssoAuthService;
    }

    @Operation(summary = "Authenticate with email and password")
    @ApiResponse(responseCode = "200", description = "Login succeeded or MFA challenge issued")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        LoginResult result = authService.login(request);
        if (result.mfaRequired()) {
            return ResponseEntity.ok(LoginResponseDTO.mfaPending(result.mfaToken()));
        }
        AuthSession session = result.session();
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.ok(LoginResponseDTO.complete(session.response()));
    }

    @Operation(summary = "Complete MFA verification after login")
    @ApiResponse(responseCode = "200", description = "Authenticated session established")
    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMfa(
            @Valid @RequestBody MfaVerifyRequestDTO request,
            HttpServletResponse response) {
        LoginResult result = authService.verifyMfa(request.mfaToken(), request.code());
        AuthSession session = result.session();
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.ok(session.response());
    }

    @Operation(summary = "List SSO providers available for an email domain")
    @ApiResponse(responseCode = "200", description = "SSO provider list")
    @GetMapping("/sso/providers")
    public ResponseEntity<SsoProvidersResponseDTO> ssoProviders(@RequestParam(required = false) String email) {
        return ResponseEntity.ok(ssoAuthService.resolveProvidersForEmail(email));
    }

    @Operation(summary = "Refresh access token using refresh cookie")
    @ApiResponse(responseCode = "204", description = "Tokens refreshed")
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.readRefreshToken(request);
        AuthSession session = authService.refresh(refreshToken);
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Logout and clear auth cookies")
    @ApiResponse(responseCode = "204", description = "Session ended")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(authCookieService.readRefreshToken(request));
        authCookieService.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Register a new organization and admin user")
    @ApiResponse(responseCode = "200", description = "Organization created and session established")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequestDTO request,
            HttpServletResponse response) {
        AuthSession session = organizationProvisioningService.signup(request);
        authCookieService.writeAuthCookies(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.ok(session.response());
    }

    @Operation(summary = "Request a password reset email")
    @ApiResponse(responseCode = "204", description = "Reset email queued if account exists")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset password using a reset token")
    @ApiResponse(responseCode = "204", description = "Password updated")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
