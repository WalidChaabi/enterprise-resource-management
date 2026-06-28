package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByProjectIdAndStatus(UUID projectId, com.company.erm.entity.TaskStatus status);

    List<Task> findByAssignedToId(UUID assignedToId);

    Optional<Task> findByIdAndProjectOrganizationId(UUID id, UUID organizationId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.project.organization.id = :organizationId")
    List<Task> findByProjectIdAndOrganizationId(@Param("projectId") UUID projectId, @Param("organizationId") UUID organizationId);

    @Query("SELECT t FROM Task t WHERE t.project.organization.id = :organizationId")
    List<Task> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT t FROM Task t WHERE t.project.organization.id = :orgId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Task> searchByOrganizationIdAndQuery(
            @Param("orgId") UUID orgId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.organization.id = :orgId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    long countSearchByOrganizationIdAndQuery(@Param("orgId") UUID orgId, @Param("query") String query);
}

