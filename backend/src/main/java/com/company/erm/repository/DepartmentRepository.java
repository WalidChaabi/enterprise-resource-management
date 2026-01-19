package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Department;
import com.company.erm.entity.Organization;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByOrganization(Organization organization);
    
    Optional<Department> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    List<Department> findByOrganizationId(UUID organizationId);
    
    Optional<Department> findByOrganizationAndCode(Organization organization, String code);
    
    @Query("SELECT d FROM Department d WHERE d.organization.id = :organizationId " +
           "AND (:active IS NULL OR d.active = :active) " +
           "AND (:parentId IS NULL OR d.parent.id = :parentId)")
    Page<Department> findByOrganizationIdAndFilters(
        @Param("organizationId") UUID organizationId,
        @Param("active") Boolean active,
        @Param("parentId") UUID parentId,
        Pageable pageable
    );
}


