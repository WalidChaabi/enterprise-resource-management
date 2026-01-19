package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = {"members", "manager"})
    List<Project> findByOrganizationIdAndArchivedFalse(UUID organizationId);

    @EntityGraph(attributePaths = {"members", "manager", "organization"})
    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = {"members", "manager"})
    List<Project> findByOrganizationId(UUID organizationId);
}