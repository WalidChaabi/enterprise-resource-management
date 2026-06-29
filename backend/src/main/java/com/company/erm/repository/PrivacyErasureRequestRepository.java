package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.PrivacyErasureRequest;
import com.company.erm.entity.PrivacyErasureStatus;

public interface PrivacyErasureRequestRepository extends JpaRepository<PrivacyErasureRequest, UUID> {

    Optional<PrivacyErasureRequest> findByUserIdAndStatus(UUID userId, PrivacyErasureStatus status);

    List<PrivacyErasureRequest> findByOrganizationIdAndStatusOrderByRequestedAtDesc(
            UUID organizationId,
            PrivacyErasureStatus status);
}
