package com.company.erm.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.company.erm.entity.ProjectStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = {"members", "manager"})
    List<Project> findByOrganizationIdAndArchivedFalse(UUID organizationId);

    @EntityGraph(attributePaths = {"members", "manager", "organization"})
    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = {"members", "manager"})
    List<Project> findByOrganizationId(UUID organizationId);

    @EntityGraph(attributePaths = {"members", "manager"})
    @Query("SELECT p FROM Project p WHERE p.organization.id = :orgId AND p.archived = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Project> searchByOrganizationIdAndQuery(
            @Param("orgId") UUID orgId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.organization.id = :orgId AND p.archived = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    long countSearchByOrganizationIdAndQuery(@Param("orgId") UUID orgId, @Param("query") String query);

    long countByOrganizationIdAndArchivedFalse(UUID organizationId);

    long countByOrganizationIdAndArchivedFalseAndStatus(UUID organizationId, ProjectStatus status);

    @Query("SELECT COALESCE(SUM(p.budget), 0) FROM Project p WHERE p.organization.id = :orgId AND p.archived = false")
    BigDecimal sumBudgetByOrganizationIdAndArchivedFalse(@Param("orgId") UUID orgId);

    @Query("SELECT COALESCE(SUM(p.cost), 0) FROM Project p WHERE p.organization.id = :orgId AND p.archived = false")
    BigDecimal sumCostByOrganizationIdAndArchivedFalse(@Param("orgId") UUID orgId);
}