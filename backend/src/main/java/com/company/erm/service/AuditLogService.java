package com.company.erm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for audit logging.
 * 
 * In production, this should write to:
 * - Database audit table
 * - Log aggregation system (ELK, Splunk)
 * - Event streaming (Kafka) for real-time monitoring
 */
@Service
public class AuditLogService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private final ObjectMapper objectMapper;
    private final AuditEventService auditEventService;
    
    public AuditLogService(ObjectMapper objectMapper, AuditEventService auditEventService) {
        this.objectMapper = objectMapper;
        this.auditEventService = auditEventService;
    }
    
    /**
     * Log employee creation.
     */
    public void logEmployeeCreated(
            UUID organizationId,
            UUID employeeId,
            String employeeEmail,
            String createdBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("employeeEmail", employeeEmail);
        details.put("action", "CREATE");
        
        AuditEvent event = new AuditEvent(
            "EMPLOYEE_CREATED",
            organizationId,
            "EMPLOYEE",
            employeeId,
            createdBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log employee update with change tracking.
     */
    public void logEmployeeUpdated(
            UUID organizationId,
            UUID employeeId,
            String modifiedBy,
            String oldEmail, String newEmail,
            UUID oldDepartmentId, UUID newDepartmentId,
            boolean oldActive, boolean newActive) {
        
        Map<String, Object> changes = new HashMap<>();
        
        if (!oldEmail.equals(newEmail)) {
            Map<String, String> emailChange = new HashMap<>();
            emailChange.put("old", oldEmail);
            emailChange.put("new", newEmail);
            changes.put("email", emailChange);
        }
        if (oldDepartmentId != null && newDepartmentId != null && !oldDepartmentId.equals(newDepartmentId)) {
            Map<String, String> deptChange = new HashMap<>();
            deptChange.put("old", oldDepartmentId.toString());
            deptChange.put("new", newDepartmentId.toString());
            changes.put("departmentId", deptChange);
        }
        if (oldActive != newActive) {
            Map<String, Boolean> activeChange = new HashMap<>();
            activeChange.put("old", oldActive);
            activeChange.put("new", newActive);
            changes.put("active", activeChange);
        }
        
        AuditEvent event = new AuditEvent(
            "EMPLOYEE_UPDATED",
            organizationId,
            "EMPLOYEE",
            employeeId,
            modifiedBy,
            LocalDateTime.now(),
            changes
        );
        
        logEvent(event);
    }
    
    /**
     * Log employee deactivation.
     */
    public void logEmployeeDeactivated(
            UUID organizationId,
            UUID employeeId,
            String deactivatedBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("action", "DEACTIVATE");
        
        AuditEvent event = new AuditEvent(
            "EMPLOYEE_DEACTIVATED",
            organizationId,
            "EMPLOYEE",
            employeeId,
            deactivatedBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log department creation.
     */
    public void logDepartmentCreated(
            UUID organizationId,
            UUID departmentId,
            String departmentCode,
            String createdBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("departmentCode", departmentCode);
        details.put("action", "CREATE");
        
        AuditEvent event = new AuditEvent(
            "DEPARTMENT_CREATED",
            organizationId,
            "DEPARTMENT",
            departmentId,
            createdBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log department update with change tracking.
     */
    public void logDepartmentUpdated(
            UUID organizationId,
            UUID departmentId,
            String modifiedBy,
            String oldCode, String newCode,
            UUID oldParentId, UUID newParentId,
            boolean oldActive, boolean newActive) {
        
        Map<String, Object> changes = new HashMap<>();
        
        if (!oldCode.equals(newCode)) {
            Map<String, String> codeChange = new HashMap<>();
            codeChange.put("old", oldCode);
            codeChange.put("new", newCode);
            changes.put("code", codeChange);
        }
        
        if (oldParentId == null && newParentId != null) {
            Map<String, String> parentChange = new HashMap<>();
            parentChange.put("old", null);
            parentChange.put("new", newParentId.toString());
            changes.put("parentId", parentChange);
        } else if (oldParentId != null && newParentId == null) {
            Map<String, String> parentChange = new HashMap<>();
            parentChange.put("old", oldParentId.toString());
            parentChange.put("new", null);
            changes.put("parentId", parentChange);
        } else if (oldParentId != null && newParentId != null && !oldParentId.equals(newParentId)) {
            Map<String, String> parentChange = new HashMap<>();
            parentChange.put("old", oldParentId.toString());
            parentChange.put("new", newParentId.toString());
            changes.put("parentId", parentChange);
        }
        
        if (oldActive != newActive) {
            Map<String, Boolean> activeChange = new HashMap<>();
            activeChange.put("old", oldActive);
            activeChange.put("new", newActive);
            changes.put("active", activeChange);
        }
        
        AuditEvent event = new AuditEvent(
            "DEPARTMENT_UPDATED",
            organizationId,
            "DEPARTMENT",
            departmentId,
            modifiedBy,
            LocalDateTime.now(),
            changes
        );
        
        logEvent(event);
    }
    
    /**
     * Log department deactivation.
     */
    public void logDepartmentDeactivated(
            UUID organizationId,
            UUID departmentId,
            String deactivatedBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("action", "DEACTIVATE");
        
        AuditEvent event = new AuditEvent(
            "DEPARTMENT_DEACTIVATED",
            organizationId,
            "DEPARTMENT",
            departmentId,
            deactivatedBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log role creation.
     */
    public void logRoleCreated(
            UUID organizationId,
            UUID roleId,
            String roleCode,
            String createdBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("roleCode", roleCode);
        details.put("action", "CREATE");
        
        AuditEvent event = new AuditEvent(
            "ROLE_CREATED",
            organizationId,
            "ROLE",
            roleId,
            createdBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log role update with change tracking.
     */
    public void logRoleUpdated(
            UUID organizationId,
            UUID roleId,
            String modifiedBy,
            String oldName, String newName,
            String oldScope, String newScope) {
        
        Map<String, Object> changes = new HashMap<>();
        
        if (!oldName.equals(newName)) {
            Map<String, String> nameChange = new HashMap<>();
            nameChange.put("old", oldName);
            nameChange.put("new", newName);
            changes.put("name", nameChange);
        }
        
        if (!oldScope.equals(newScope)) {
            Map<String, String> scopeChange = new HashMap<>();
            scopeChange.put("old", oldScope);
            scopeChange.put("new", newScope);
            changes.put("scope", scopeChange);
        }
        
        AuditEvent event = new AuditEvent(
            "ROLE_UPDATED",
            organizationId,
            "ROLE",
            roleId,
            modifiedBy,
            LocalDateTime.now(),
            changes
        );
        
        logEvent(event);
    }
    
    /**
     * Log role deletion.
     */
    public void logRoleDeleted(
            UUID organizationId,
            UUID roleId,
            String roleCode,
            String deletedBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("roleCode", roleCode);
        details.put("action", "DELETE");
        
        AuditEvent event = new AuditEvent(
            "ROLE_DELETED",
            organizationId,
            "ROLE",
            roleId,
            deletedBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log role assignment to user.
     */
    public void logRoleAssigned(
            UUID organizationId,
            UUID userId,
            String userEmail,
            UUID roleId,
            String roleCode,
            String assignedBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId.toString());
        details.put("userEmail", userEmail);
        details.put("roleId", roleId.toString());
        details.put("roleCode", roleCode);
        details.put("action", "ASSIGN");
        
        AuditEvent event = new AuditEvent(
            "ROLE_ASSIGNED",
            organizationId,
            "USER_ROLE",
            userId,
            assignedBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    /**
     * Log role removal from user.
     */
    public void logRoleRemoved(
            UUID organizationId,
            UUID userId,
            String userEmail,
            UUID roleId,
            String roleCode,
            String removedBy) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId.toString());
        details.put("userEmail", userEmail);
        details.put("roleId", roleId.toString());
        details.put("roleCode", roleCode);
        details.put("action", "REMOVE");
        
        AuditEvent event = new AuditEvent(
            "ROLE_REMOVED",
            organizationId,
            "USER_ROLE",
            userId,
            removedBy,
            LocalDateTime.now(),
            details
        );
        
        logEvent(event);
    }
    
    private void logEvent(AuditEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            auditLogger.info("AUDIT_EVENT: {}", json);
            auditEventService.persist(event);
        } catch (Exception e) {
            auditLogger.error("Failed to log audit event", e);
        }
    }
    
    /**
     * Audit event data structure.
     */
    public record AuditEvent(
        String eventType,
        UUID organizationId,
        String entityType,
        UUID entityId,
        String actor,
        LocalDateTime timestamp,
        Map<String, Object> details
    ) {}
}


