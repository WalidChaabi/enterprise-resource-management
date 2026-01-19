package com.company.erm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByEmployeeId(UUID employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatus(UUID employeeId, LeaveRequestStatus status);

    List<LeaveRequest> findByEmployeeIdAndStartDateBetween(
        UUID employeeId,
        LocalDate startDate,
        LocalDate endDate
    );

    Optional<LeaveRequest> findByIdAndEmployeeOrganizationId(UUID id, UUID organizationId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.organization.id = :organizationId")
    List<LeaveRequest> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :departmentId")
    List<LeaveRequest> findByDepartmentId(@Param("departmentId") UUID departmentId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.organization.id = :organizationId " +
           "AND lr.startDate <= :date AND lr.endDate >= :date")
    List<LeaveRequest> findOverlappingRequests(
        @Param("organizationId") UUID organizationId,
        @Param("date") LocalDate date
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status = 'APPROVED' " +
           "AND ((lr.startDate <= :startDate AND lr.endDate >= :startDate) OR " +
           "     (lr.startDate <= :endDate AND lr.endDate >= :endDate) OR " +
           "     (lr.startDate >= :startDate AND lr.endDate <= :endDate))")
    List<LeaveRequest> findOverlappingApprovedRequests(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

