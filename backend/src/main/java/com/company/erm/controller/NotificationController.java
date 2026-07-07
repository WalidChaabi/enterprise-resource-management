package com.company.erm.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.erm.dto.NotificationResponseDTO;
import com.company.erm.service.NotificationService;

/**
 * REST controller for user notifications.
 * 
 * Endpoints:
 * - GET /api/notifications
 * - POST /api/notifications/mark-all-read
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notifications", description = "In-app notifications")
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * List current user's notifications with pagination.
     */
    @Operation(summary = "List Notifications")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        Page<NotificationResponseDTO> notifications = notificationService
            .getCurrentUserNotifications(currentUserEmail, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark all notifications as read for current user.
     */
    @Operation(summary = "Mark All Read")
    @ApiResponse(responseCode = "200", description = "Success")
    @PostMapping("/mark-all-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        notificationService.markAllReadForCurrentUser(currentUserEmail);
        return ResponseEntity.noContent().build();
    }
}
