package com.company.erm.service;

import com.company.erm.dto.AuthRequest;
import com.company.erm.dto.AuthResponse;
import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.JwtTokenProvider;
import com.company.erm.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * 
 * Tests critical authentication paths:
 * - Successful login
 * - Invalid credentials
 * - Inactive user
 * - JWT token generation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private PasswordEncoder realPasswordEncoder;
    private Organization organization;
    private User testUser;
    private Role testRole;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        realPasswordEncoder = new BCryptPasswordEncoder();
        organization = TestDataFactory.createOrganization();
        testUser = TestDataFactory.createUser(organization, null, "test@example.com", "password123", realPasswordEncoder);
        testRole = TestDataFactory.createRole(organization, "HR_ADMIN", "HR Administrator");
        userRole = TestDataFactory.createUserRole(testUser, testRole);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_shouldReturnToken_whenCredentialsValid() {
        // Given
        AuthRequest request = new AuthRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUser(testUser)).thenReturn(List.of(userRole));
        when(tokenProvider.generateToken(any(), any(), any())).thenReturn("test-jwt-token");
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
        when(refreshTokenService.issueRefreshToken(testUser)).thenReturn("refresh-token");

        // When
        AuthResponse response = authService.login(request).response();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("test-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.roles()).contains("HR Administrator");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRoleRepository).findByUser(testUser);
        verify(tokenProvider).generateToken(testUser.getId(), testUser.getEmail(), organization.getId());
        verify(passwordEncoder).matches("password123", testUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void login_shouldThrowException_whenUserNotFound() {
        // Given
        AuthRequest request = new AuthRequest("nonexistent@example.com", "password123");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRoleRepository, never()).findByUser(any());
        verify(tokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void login_shouldThrowException_whenPasswordIncorrect() {
        // Given
        AuthRequest request = new AuthRequest("test@example.com", "wrongpassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPasswordHash());
        verify(userRoleRepository, never()).findByUser(any());
        verify(tokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when user is inactive")
    void login_shouldThrowException_whenUserInactive() {
        // Given
        User inactiveUser = TestDataFactory.createInactiveUser(organization, null, passwordEncoder);
        inactiveUser.setEmail("inactive@example.com");
        AuthRequest request = new AuthRequest("inactive@example.com", "password123");
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User account is inactive");

        verify(userRepository).findByEmail("inactive@example.com");
        verify(userRoleRepository, never()).findByUser(any());
        verify(tokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Should include all user roles in response")
    void login_shouldIncludeAllRoles_whenUserHasMultipleRoles() {
        // Given
        Role role1 = TestDataFactory.createRole(organization, "HR_ADMIN", "HR Administrator");
        Role role2 = TestDataFactory.createRole(organization, "ORG_ADMIN", "Organization Administrator");
        UserRole userRole1 = TestDataFactory.createUserRole(testUser, role1);
        UserRole userRole2 = TestDataFactory.createUserRole(testUser, role2);

        AuthRequest request = new AuthRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUser(testUser)).thenReturn(List.of(userRole1, userRole2));
        when(tokenProvider.generateToken(any(), any(), any())).thenReturn("test-jwt-token");
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
        when(refreshTokenService.issueRefreshToken(testUser)).thenReturn("refresh-token");

        // When
        AuthResponse response = authService.login(request).response();

        // Then
        assertThat(response.roles()).hasSize(2);
        assertThat(response.roles()).contains("HR Administrator", "Organization Administrator");
    }
}
