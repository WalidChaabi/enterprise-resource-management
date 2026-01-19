package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findByUser(User user);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.organization.id = :organizationId")
    List<UserRole> findByUserIdAndOrganizationId(
        @Param("userId") UUID userId,
        @Param("organizationId") UUID organizationId
    );
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId AND ur.organization.id = :organizationId")
    Optional<UserRole> findByUserIdAndRoleIdAndOrganizationId(
        @Param("userId") UUID userId,
        @Param("roleId") UUID roleId,
        @Param("organizationId") UUID organizationId
    );
}


