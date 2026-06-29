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

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
    
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

    @EntityGraph(attributePaths = {"department"})
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.active = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchActiveByOrganizationIdAndQuery(
            @Param("orgId") UUID orgId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId AND u.active = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    long countSearchActiveByOrganizationIdAndQuery(@Param("orgId") UUID orgId, @Param("query") String query);

    long countByOrganizationIdAndActiveTrue(UUID organizationId);
}


