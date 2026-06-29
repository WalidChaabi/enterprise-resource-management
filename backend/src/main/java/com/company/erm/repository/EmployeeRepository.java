package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByUserId(UUID userId);

    List<Employee> findByIdIn(Set<UUID> ids);
    
    @Query("SELECT e FROM Employee e WHERE e.id IN :ids AND e.organization.id = :organizationId")
    List<Employee> findByIdInAndOrganizationId(@Param("ids") Set<UUID> ids, @Param("organizationId") UUID organizationId);
}
