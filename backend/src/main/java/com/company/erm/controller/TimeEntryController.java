package com.company.erm.controller;

import com.company.erm.dto.TimeEntryCreateDTO;
import com.company.erm.dto.TimeEntryUpdateDTO;
import com.company.erm.dto.TimeEntryResponseDTO;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.service.TimeEntryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for time entry management.
 * 
 * Security:
 * - All endpoints require authentication
 * - Multi-tenant isolation enforced at service layer
 * - Role-based access control via @PreAuthorize
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/time-entries")
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN', 'EMPLOYEE')")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    /**
     * Create a new time entry.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN, EMPLOYEE
     * 
     * @param organizationId Organization ID
     * @param createDTO Time entry creation data
     * @param authentication Current authenticated user
     * @return Created time entry with 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TimeEntryResponseDTO> createTimeEntry(
            @PathVariable UUID organizationId,
            @Valid @RequestBody TimeEntryCreateDTO createDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TimeEntryResponseDTO created = timeEntryService.createTimeEntry(
            organizationId,
            createDTO,
            currentUserEmail
        );
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Get time entry by ID.
     * 
     * Permissions: 
     * - Employees: their own entries
     * - Managers: any entry in organization
     * 
     * @param organizationId Organization ID
     * @param timeEntryId Time entry ID
     * @param authentication Current authenticated user
     * @return Time entry details
     */
    @GetMapping("/{timeEntryId}")
    public ResponseEntity<TimeEntryResponseDTO> getTimeEntry(
            @PathVariable UUID organizationId,
            @PathVariable UUID timeEntryId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TimeEntryResponseDTO timeEntry = timeEntryService.getTimeEntry(
            organizationId,
            timeEntryId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(timeEntry);
    }

    /**
     * Update a time entry.
     * 
     * Permissions: Employees can update their own draft/submitted entries
     * 
     * @param organizationId Organization ID
     * @param timeEntryId Time entry ID
     * @param updateDTO Update data
     * @param authentication Current authenticated user
     * @return Updated time entry
     */
    @PutMapping("/{timeEntryId}")
    public ResponseEntity<TimeEntryResponseDTO> updateTimeEntry(
            @PathVariable UUID organizationId,
            @PathVariable UUID timeEntryId,
            @Valid @RequestBody TimeEntryUpdateDTO updateDTO,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TimeEntryResponseDTO updated = timeEntryService.updateTimeEntry(
            organizationId,
            timeEntryId,
            updateDTO,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * List time entries with optional filters.
     * 
     * Permissions:
     * - Employees: their own entries
     * - Managers: all entries in organization
     * 
     * @param organizationId Organization ID
     * @param employeeId Optional employee filter
     * @param projectId Optional project filter
     * @param taskId Optional task filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param status Optional status filter
     * @param authentication Current authenticated user
     * @return List of time entries
     */
    @GetMapping
    public ResponseEntity<List<TimeEntryResponseDTO>> listTimeEntries(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TimeEntryStatus status,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        List<TimeEntryResponseDTO> entries = timeEntryService.listTimeEntries(
            organizationId,
            employeeId,
            projectId,
            taskId,
            startDate,
            endDate,
            status,
            currentUserEmail
        );
        
        return ResponseEntity.ok(entries);
    }

    /**
     * Submit time entry for approval.
     * 
     * Permissions: Employees can submit their own entries
     * 
     * @param organizationId Organization ID
     * @param timeEntryId Time entry ID
     * @param authentication Current authenticated user
     * @return Updated time entry
     */
    @PostMapping("/{timeEntryId}/submit")
    public ResponseEntity<TimeEntryResponseDTO> submitTimeEntry(
            @PathVariable UUID organizationId,
            @PathVariable UUID timeEntryId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TimeEntryResponseDTO updated = timeEntryService.submitTimeEntry(
            organizationId,
            timeEntryId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Approve a time entry.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN only
     * 
     * @param organizationId Organization ID
     * @param timeEntryId Time entry ID
     * @param authentication Current authenticated user
     * @return Approved time entry
     */
    @PostMapping("/{timeEntryId}/approve")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<TimeEntryResponseDTO> approveTimeEntry(
            @PathVariable UUID organizationId,
            @PathVariable UUID timeEntryId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TimeEntryResponseDTO updated = timeEntryService.approveTimeEntry(
            organizationId,
            timeEntryId,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Reject a time entry.
     * 
     * Permissions: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN only
     * 
     * @param organizationId Organization ID
     * @param timeEntryId Time entry ID
     * @param rejectionReason Reason for rejection
     * @param authentication Current authenticated user
     * @return Rejected time entry
     */
    @PostMapping("/{timeEntryId}/reject")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')")
    public ResponseEntity<TimeEntryResponseDTO> rejectTimeEntry(
            @PathVariable UUID organizationId,
            @PathVariable UUID timeEntryId,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        TimeEntryResponseDTO updated = timeEntryService.rejectTimeEntry(
            organizationId,
            timeEntryId,
            rejectionReason,
            currentUserEmail
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a time entry.
     * 
     * Permissions: Employees can delete their own draft entries
     * 
     * @param organizationId Organization ID
     * @param timeEntryId Time entry ID
     * @param authentication Current authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/{timeEntryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTimeEntry(
            @PathVariable UUID organizationId,
            @PathVariable UUID timeEntryId,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        timeEntryService.deleteTimeEntry(
            organizationId,
            timeEntryId,
            currentUserEmail
        );
        
        return ResponseEntity.noContent().build();
    }
}

