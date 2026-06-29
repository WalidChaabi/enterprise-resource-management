package com.company.erm.dto.privacy;

import java.time.LocalDateTime;
import java.util.UUID;

import com.company.erm.entity.PrivacyErasureStatus;

public class ErasureRequestResponseDTO {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private PrivacyErasureStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String adminNotes;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public PrivacyErasureStatus getStatus() {
        return status;
    }

    public void setStatus(PrivacyErasureStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}
