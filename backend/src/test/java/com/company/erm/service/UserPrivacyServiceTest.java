package com.company.erm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.company.erm.entity.Organization;
import com.company.erm.entity.PrivacyErasureRequest;
import com.company.erm.entity.PrivacyErasureStatus;
import com.company.erm.entity.Role;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.NotificationRepository;
import com.company.erm.repository.PasswordResetTokenRepository;
import com.company.erm.repository.PrivacyErasureRequestRepository;
import com.company.erm.repository.RefreshTokenRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserNotificationPreferencesRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.PermissionChecker;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserPrivacyServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private UserNotificationPreferencesRepository notificationPreferencesRepository;
    @Mock private TimeEntryRepository timeEntryRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private PrivacyErasureRequestRepository erasureRequestRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PermissionChecker permissionChecker;
    @Mock private AuditLogService auditLogService;

    private UserPrivacyService userPrivacyService;
    private User user;
    private Organization organization;

    @BeforeEach
    void setUp() {
        userPrivacyService = new UserPrivacyService(
                userRepository,
                employeeRepository,
                userRoleRepository,
                notificationPreferencesRepository,
                timeEntryRepository,
                leaveRequestRepository,
                notificationRepository,
                erasureRequestRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                permissionChecker,
                auditLogService,
                new ObjectMapper());

        organization = new Organization();
        organization.setId(UUID.randomUUID());

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("employee@example.com");
        user.setFirstName("Alex");
        user.setLastName("Smith");
        user.setOrganization(organization);
        user.setActive(true);
    }

    @Test
    void exportPersonalData_returnsZipArchive() {
        when(userRepository.findByEmail("employee@example.com")).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), organization.getId()))
                .thenReturn(List.of());
        when(timeEntryRepository.findByEmployeeId(user.getId())).thenReturn(List.of());
        when(leaveRequestRepository.findByEmployeeId(user.getId())).thenReturn(List.of());
        when(notificationRepository.findByUserIdOrderByTimeDesc(eq(user.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<byte[]> response = userPrivacyService.exportPersonalData("employee@example.com");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
        verify(auditLogService).logDataExport(organization.getId(), user.getId(), "employee@example.com");
    }

    @Test
    void requestErasure_createsPendingRequest() {
        when(userRepository.findByEmail("employee@example.com")).thenReturn(Optional.of(user));
        when(erasureRequestRepository.findByUserIdAndStatus(user.getId(), PrivacyErasureStatus.PENDING))
                .thenReturn(Optional.empty());
        when(erasureRequestRepository.save(any(PrivacyErasureRequest.class))).thenAnswer(invocation -> {
            PrivacyErasureRequest request = invocation.getArgument(0);
            request.setId(UUID.randomUUID());
            return request;
        });

        var response = userPrivacyService.requestErasure("employee@example.com");

        assertThat(response.getStatus()).isEqualTo(PrivacyErasureStatus.PENDING);
        verify(auditLogService).logErasureRequested(organization.getId(), user.getId(), "employee@example.com");
    }

    @Test
    void processEmployeeErasure_anonymizesUser() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@example.com");
        admin.setOrganization(organization);

        when(permissionChecker.hasRole(organization.getId(), "admin@example.com", "ORG_ADMIN")).thenReturn(true);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), organization.getId()))
                .thenReturn(List.of());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(erasureRequestRepository.save(any(PrivacyErasureRequest.class))).thenAnswer(invocation -> {
            PrivacyErasureRequest request = invocation.getArgument(0);
            request.setId(UUID.randomUUID());
            return request;
        });

        userPrivacyService.processEmployeeErasure(organization.getId(), user.getId(), "admin@example.com", "offboarding");

        assertThat(user.getEmail()).endsWith("@anonymized.local");
        assertThat(user.getFirstName()).isEqualTo("Deleted");
        assertThat(user.isActive()).isFalse();
        verify(refreshTokenRepository).revokeAllActiveForUser(eq(user.getId()), any());
        verify(auditLogService).logErasureCompleted(
                eq(organization.getId()), eq(user.getId()), eq("admin@example.com"), eq("offboarding"));
    }

    @Test
    void processEmployeeErasure_blocksLastOrgAdmin() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@example.com");
        admin.setOrganization(organization);

        Role orgAdminRole = new Role();
        orgAdminRole.setCode("ORG_ADMIN");
        UserRole userRole = new UserRole();
        userRole.setRole(orgAdminRole);

        when(permissionChecker.hasRole(organization.getId(), "admin@example.com", "ORG_ADMIN")).thenReturn(true);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserIdAndOrganizationId(user.getId(), organization.getId()))
                .thenReturn(List.of(userRole));
        when(userRoleRepository.countActiveOrgAdmins(organization.getId())).thenReturn(1L);

        assertThatThrownBy(() -> userPrivacyService.processEmployeeErasure(
                        organization.getId(), user.getId(), "admin@example.com", null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("LAST_ORG_ADMIN");
    }
}
