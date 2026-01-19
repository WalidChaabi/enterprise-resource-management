package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Organization;
import com.company.erm.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @EntityGraph(attributePaths = {"organization"})
    Optional<Role> findByOrganizationAndCode(Organization organization, String code);

    List<Role> findByOrganizationOrScope(Organization organization, String scope);
    
    @EntityGraph(attributePaths = {"organization"})
    @Query("SELECT r FROM Role r WHERE r.organization = :organization OR (r.scope = :scope AND r.organization IS NULL)")
    Page<Role> findByOrganizationOrScope(
        @Param("organization") Organization organization,
        @Param("scope") String scope,
        Pageable pageable
    );
    
    @EntityGraph(attributePaths = {"organization"})
    @Query("SELECT r FROM Role r WHERE r.organization.id = :organizationId")
    Page<Role> findByOrganizationId(
        @Param("organizationId") UUID organizationId,
        Pageable pageable
    );
    
    @EntityGraph(attributePaths = {"organization"})
    Optional<Role> findByIdAndOrganizationId(UUID id, UUID organizationId);
}


