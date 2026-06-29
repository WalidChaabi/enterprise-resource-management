package com.company.erm.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query("""
            SELECT e FROM AuditEvent e
            WHERE e.organizationId = :organizationId
              AND (:action IS NULL OR e.action = :action)
              AND (:entityType IS NULL OR e.entityType = :entityType)
            ORDER BY e.createdAt DESC
            """)
    Page<AuditEvent> findByOrganizationWithFilters(
            @Param("organizationId") UUID organizationId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            Pageable pageable);
}
