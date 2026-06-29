package com.company.erm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.WebhookEndpoint;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    List<WebhookEndpoint> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<WebhookEndpoint> findByOrganizationIdAndActiveTrue(UUID organizationId);

    java.util.Optional<WebhookEndpoint> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
