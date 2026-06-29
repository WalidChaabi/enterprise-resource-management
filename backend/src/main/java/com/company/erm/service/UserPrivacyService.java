package com.company.erm.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.privacy.ErasureRequestResponseDTO;
import com.company.erm.entity.Employee;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.Notification;
import com.company.erm.entity.PrivacyErasureRequest;
import com.company.erm.entity.PrivacyErasureStatus;
import com.company.erm.entity.TimeEntry;
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
import com.company.erm.security.RoleCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
@Transactional
public class UserPrivacyService {

    private static final String ANONYMIZED_EMAIL_SUFFIX = "@anonymized.local";

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserNotificationPreferencesRepository notificationPreferencesRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;
    private final PrivacyErasureRequestRepository erasureRequestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public UserPrivacyService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            UserRoleRepository userRoleRepository,
            UserNotificationPreferencesRepository notificationPreferencesRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            NotificationRepository notificationRepository,
            PrivacyErasureRequestRepository erasureRequestRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            PermissionChecker permissionChecker,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.userRoleRepository = userRoleRepository;
        this.notificationPreferencesRepository = notificationPreferencesRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
        this.erasureRequestRepository = erasureRequestRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionChecker = permissionChecker;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportPersonalData(String currentUserEmail) {
        User user = requireCurrentUser(currentUserEmail);
        Map<String, Object> export = buildExportPayload(user);
        auditLogService.logDataExport(user.getOrganization().getId(), user.getId(), currentUserEmail);

        try {
            byte[] zipBytes = toZip(export);
            String filename = "erm-data-export-" + user.getId() + ".zip";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipBytes.length);
            return ResponseEntity.ok().headers(headers).body(zipBytes);
        } catch (Exception ex) {
            throw new BusinessException(
                    "DATA_EXPORT_FAILED",
                    "Failed to generate data export",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ErasureRequestResponseDTO requestErasure(String currentUserEmail) {
        User user = requireCurrentUser(currentUserEmail);
        ensureNotAlreadyAnonymized(user);
        erasureRequestRepository.findByUserIdAndStatus(user.getId(), PrivacyErasureStatus.PENDING)
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "ERASURE_REQUEST_PENDING",
                            "A deletion request is already pending review",
                            HttpStatus.CONFLICT);
                });

        PrivacyErasureRequest request = new PrivacyErasureRequest();
        request.setOrganization(user.getOrganization());
        request.setUser(user);
        request.setStatus(PrivacyErasureStatus.PENDING);
        PrivacyErasureRequest saved = erasureRequestRepository.save(request);

        auditLogService.logErasureRequested(
                user.getOrganization().getId(),
                user.getId(),
                currentUserEmail);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ErasureRequestResponseDTO getErasureRequest(String currentUserEmail) {
        User user = requireCurrentUser(currentUserEmail);
        return erasureRequestRepository
                .findByUserIdAndStatus(user.getId(), PrivacyErasureStatus.PENDING)
                .map(this::toDto)
                .orElseGet(() -> erasureRequestRepository
                        .findByUserIdAndStatus(user.getId(), PrivacyErasureStatus.COMPLETED)
                        .map(this::toDto)
                        .orElse(null));
    }

    @Transactional(readOnly = true)
    public List<ErasureRequestResponseDTO> listPendingErasureRequests(
            UUID organizationId,
            String currentUserEmail) {
        requireOrgAdmin(organizationId, currentUserEmail);
        return erasureRequestRepository
                .findByOrganizationIdAndStatusOrderByRequestedAtDesc(organizationId, PrivacyErasureStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ErasureRequestResponseDTO approveErasureRequest(
            UUID organizationId,
            UUID requestId,
            String currentUserEmail,
            String adminNotes) {
        User admin = requireOrgAdmin(organizationId, currentUserEmail);
        PrivacyErasureRequest request = requirePendingRequest(organizationId, requestId);
        ensureCanEraseUser(admin, request.getUser());

        executeErasure(request.getUser(), admin, adminNotes);
        request.setStatus(PrivacyErasureStatus.COMPLETED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(admin);
        request.setAdminNotes(adminNotes);
        return toDto(erasureRequestRepository.save(request));
    }

    public ErasureRequestResponseDTO rejectErasureRequest(
            UUID organizationId,
            UUID requestId,
            String currentUserEmail,
            String adminNotes) {
        User admin = requireOrgAdmin(organizationId, currentUserEmail);
        PrivacyErasureRequest request = requirePendingRequest(organizationId, requestId);
        if (request.getUser().getId().equals(admin.getId())) {
            throw new BusinessException(
                    "ERASURE_SELF_REVIEW",
                    "You cannot review your own deletion request",
                    HttpStatus.FORBIDDEN);
        }

        request.setStatus(PrivacyErasureStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(admin);
        request.setAdminNotes(adminNotes);
        return toDto(erasureRequestRepository.save(request));
    }

    public ErasureRequestResponseDTO processEmployeeErasure(
            UUID organizationId,
            UUID userId,
            String currentUserEmail,
            String adminNotes) {
        User admin = requireOrgAdminOrHrAdmin(organizationId, currentUserEmail);
        User target = userRepository.findById(userId)
                .filter(user -> user.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "User not found in this organization",
                        HttpStatus.NOT_FOUND));
        ensureCanEraseUser(admin, target);
        ensureNotAlreadyAnonymized(target);

        executeErasure(target, admin, adminNotes);

        PrivacyErasureRequest request = new PrivacyErasureRequest();
        request.setOrganization(target.getOrganization());
        request.setUser(target);
        request.setStatus(PrivacyErasureStatus.COMPLETED);
        request.setRequestedAt(LocalDateTime.now());
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(admin);
        request.setAdminNotes(adminNotes);
        return toDto(erasureRequestRepository.save(request));
    }

    private void executeErasure(User user, User processedBy, String adminNotes) {
        anonymizeUser(user);
        employeeRepository.findByUserId(user.getId()).ifPresent(this::deactivateEmployee);
        refreshTokenRepository.revokeAllActiveForUser(user.getId(), Instant.now());
        passwordResetTokenRepository.invalidateActiveTokensForUser(user.getId(), Instant.now());

        auditLogService.logErasureCompleted(
                user.getOrganization().getId(),
                user.getId(),
                processedBy.getEmail(),
                adminNotes);
    }

    private void anonymizeUser(User user) {
        user.setEmail("deleted-" + user.getId() + ANONYMIZED_EMAIL_SUFFIX);
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setActive(false);
        user.setDepartment(null);
        userRepository.save(user);
    }

    private void deactivateEmployee(Employee employee) {
        employee.setActive(false);
        employee.setJobTitle(null);
        employeeRepository.save(employee);
    }

    private Map<String, Object> buildExportPayload(User user) {
        UUID userId = user.getId();
        UUID organizationId = user.getOrganization().getId();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("exportVersion", "1.0");
        payload.put("exportedAt", LocalDateTime.now());

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", userId);
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("active", user.isActive());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("updatedAt", user.getUpdatedAt());
        payload.put("user", userMap);

        employeeRepository.findByUserId(userId).ifPresent(employee -> payload.put("employee", Map.of(
                "id", employee.getId(),
                "employeeCode", nullToEmpty(employee.getEmployeeCode()),
                "jobTitle", nullToEmpty(employee.getJobTitle()),
                "active", employee.isActive(),
                "createdAt", employee.getCreatedAt(),
                "updatedAt", employee.getUpdatedAt())));

        List<Map<String, Object>> roles = userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .stream()
                .map(this::roleToMap)
                .toList();
        payload.put("roles", roles);

        notificationPreferencesRepository.findByUserId(userId).ifPresent(prefs -> payload.put(
                "notificationPreferences",
                Map.of(
                        "emailEnabled", prefs.isEmailEnabled(),
                        "inAppEnabled", prefs.isInAppEnabled())));

        payload.put("timeEntries", timeEntryRepository.findByEmployeeId(userId).stream()
                .map(this::timeEntryToMap)
                .toList());
        payload.put("leaveRequests", leaveRequestRepository.findByEmployeeId(userId).stream()
                .map(this::leaveRequestToMap)
                .toList());
        payload.put("notifications", notificationRepository
                .findByUserIdOrderByTimeDesc(userId, Pageable.ofSize(5_000))
                .getContent()
                .stream()
                .map(this::notificationToMap)
                .toList());

        return payload;
    }

    private byte[] toZip(Map<String, Object> export) throws Exception {
        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(export);
        byte[] readme = """
                ERM Personal Data Export
                ========================
                This archive contains personal data associated with your account in JSON format.
                File: personal-data.json

                For questions contact your organization administrator.
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry("personal-data.json"));
            zip.write(json);
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("README.txt"));
            zip.write(readme);
            zip.closeEntry();
        }
        return output.toByteArray();
    }

    private Map<String, Object> roleToMap(UserRole userRole) {
        Map<String, Object> map = new HashMap<>();
        map.put("roleCode", userRole.getRole().getCode());
        map.put("roleName", userRole.getRole().getName());
        map.put("assignedAt", userRole.getAssignedAt());
        return map;
    }

    private Map<String, Object> timeEntryToMap(TimeEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entry.getId());
        map.put("entryDate", entry.getEntryDate());
        map.put("startTime", entry.getStartTime());
        map.put("endTime", entry.getEndTime());
        map.put("durationHours", entry.getDurationHours());
        map.put("description", entry.getDescription());
        map.put("status", entry.getStatus());
        map.put("projectId", entry.getProject().getId());
        if (entry.getTask() != null) {
            map.put("taskId", entry.getTask().getId());
        }
        map.put("createdAt", entry.getCreatedAt());
        map.put("updatedAt", entry.getUpdatedAt());
        return map;
    }

    private Map<String, Object> leaveRequestToMap(LeaveRequest request) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", request.getId());
        map.put("leaveType", request.getLeaveType());
        map.put("startDate", request.getStartDate());
        map.put("endDate", request.getEndDate());
        map.put("totalDays", request.getTotalDays());
        map.put("reason", request.getReason());
        map.put("status", request.getStatus());
        map.put("approvedAt", request.getApprovedAt());
        map.put("rejectionReason", request.getRejectionReason());
        map.put("createdAt", request.getCreatedAt());
        map.put("updatedAt", request.getUpdatedAt());
        return map;
    }

    private Map<String, Object> notificationToMap(Notification notification) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", notification.getId());
        map.put("title", notification.getTitle());
        map.put("description", notification.getDescription());
        map.put("time", notification.getTime());
        map.put("unread", notification.isUnread());
        return map;
    }

    private PrivacyErasureRequest requirePendingRequest(UUID organizationId, UUID requestId) {
        PrivacyErasureRequest request = erasureRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(
                        "ERASURE_REQUEST_NOT_FOUND",
                        "Deletion request not found",
                        HttpStatus.NOT_FOUND));
        if (!request.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException(
                    "ERASURE_REQUEST_NOT_FOUND",
                    "Deletion request not found",
                    HttpStatus.NOT_FOUND);
        }
        if (request.getStatus() != PrivacyErasureStatus.PENDING) {
            throw new BusinessException(
                    "ERASURE_REQUEST_NOT_PENDING",
                    "Deletion request is no longer pending",
                    HttpStatus.CONFLICT);
        }
        return request;
    }

    private void ensureCanEraseUser(User actor, User target) {
        if (target.getId().equals(actor.getId())) {
            throw new BusinessException(
                    "ERASURE_SELF_NOT_ALLOWED",
                    "Use the deletion request workflow for your own account",
                    HttpStatus.FORBIDDEN);
        }
        ensureNotAlreadyAnonymized(target);
        ensureNotLastOrgAdmin(target);
    }

    private void ensureNotLastOrgAdmin(User target) {
        boolean isOrgAdmin = userRoleRepository.findByUserIdAndOrganizationId(
                        target.getId(), target.getOrganization().getId())
                .stream()
                .anyMatch(userRole -> RoleCode.ORG_ADMIN.code().equals(userRole.getRole().getCode()));
        if (!isOrgAdmin) {
            return;
        }
        long activeAdmins = userRoleRepository.countActiveOrgAdmins(target.getOrganization().getId());
        if (activeAdmins <= 1) {
            throw new BusinessException(
                    "LAST_ORG_ADMIN",
                    "Cannot erase the last active organization administrator",
                    HttpStatus.CONFLICT);
        }
    }

    private void ensureNotAlreadyAnonymized(User user) {
        if (user.getEmail() != null && user.getEmail().endsWith(ANONYMIZED_EMAIL_SUFFIX)) {
            throw new BusinessException(
                    "USER_ALREADY_ANONYMIZED",
                    "This account has already been anonymized",
                    HttpStatus.CONFLICT);
        }
    }

    private User requireCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "User not found",
                        HttpStatus.NOT_FOUND));
    }

    private User requireOrgAdmin(UUID organizationId, String currentUserEmail) {
        permissionChecker.requireRole(organizationId, currentUserEmail, RoleCode.ORG_ADMIN);
        return requireCurrentUser(currentUserEmail);
    }

    private User requireOrgAdminOrHrAdmin(UUID organizationId, String currentUserEmail) {
        boolean allowed = permissionChecker.hasRole(organizationId, currentUserEmail, RoleCode.ORG_ADMIN.code())
                || permissionChecker.hasRole(organizationId, currentUserEmail, RoleCode.HR_ADMIN.code());
        if (!allowed) {
            throw new BusinessException(
                    "ACCESS_DENIED",
                    "Administrator access required",
                    HttpStatus.FORBIDDEN);
        }
        return requireCurrentUser(currentUserEmail);
    }

    private ErasureRequestResponseDTO toDto(PrivacyErasureRequest request) {
        User user = request.getUser();
        ErasureRequestResponseDTO dto = new ErasureRequestResponseDTO();
        dto.setId(request.getId());
        dto.setUserId(user.getId());
        dto.setUserEmail(user.getEmail());
        dto.setUserFullName(user.getFullName());
        dto.setStatus(request.getStatus());
        dto.setRequestedAt(request.getRequestedAt());
        dto.setProcessedAt(request.getProcessedAt());
        dto.setAdminNotes(request.getAdminNotes());
        return dto;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
