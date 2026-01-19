package com.company.erm.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Organization;
import com.company.erm.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByOrganizationAndEmail(Organization organization, String email);
    
    @EntityGraph(attributePaths = {"organization", "department"})
    Optional<User> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"organization", "department"})
    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);
    
    @Query("SELECT u FROM User u WHERE u.organization.id = :organizationId " +
           "AND (:departmentId IS NULL OR u.department.id = :departmentId) " +
           "AND (:active IS NULL OR u.active = :active)")
    Page<User> findByOrganizationIdAndFilters(
        @Param("organizationId") UUID organizationId,
        @Param("departmentId") UUID departmentId,
        @Param("active") Boolean active,
        Pageable pageable
    );
}


