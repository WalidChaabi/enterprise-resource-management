package com.company.erm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.WebhookDelivery;
import com.company.erm.webhook.WebhookDeliveryStatus;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

    Page<WebhookDelivery> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    Page<WebhookDelivery> findByEndpointIdOrderByCreatedAtDesc(UUID endpointId, Pageable pageable);

    @Query("""
            SELECT d FROM WebhookDelivery d
            WHERE d.status IN :statuses
              AND d.attemptCount < d.maxAttempts
              AND d.nextRetryAt <= :now
            ORDER BY d.createdAt ASC
            """)
    List<WebhookDelivery> findDueForDelivery(
            @Param("statuses") List<WebhookDeliveryStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
