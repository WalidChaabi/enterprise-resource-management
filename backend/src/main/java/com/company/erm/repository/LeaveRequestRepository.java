package com.company.erm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import com.company.erm.repository.reporting.LeaveBalanceAggregate;

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

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.organization.id = :orgId AND " +
           "(LOWER(COALESCE(lr.reason, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CONCAT(lr.employee.firstName, ' ', lr.employee.lastName)) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<LeaveRequest> searchByOrganizationIdAndQuery(
            @Param("orgId") UUID orgId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.organization.id = :orgId AND " +
           "(LOWER(COALESCE(lr.reason, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CONCAT(lr.employee.firstName, ' ', lr.employee.lastName)) LIKE LOWER(CONCAT('%', :query, '%')))")
    long countSearchByOrganizationIdAndQuery(@Param("orgId") UUID orgId, @Param("query") String query);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr "
            + "WHERE lr.employee.organization.id = :orgId AND lr.status = :status")
    long countByOrganizationIdAndStatus(
            @Param("orgId") UUID orgId,
            @Param("status") LeaveRequestStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr "
            + "WHERE lr.employee.organization.id = :orgId AND lr.status = 'APPROVED' "
            + "AND lr.startDate >= :start AND lr.startDate <= :end")
    long countUpcomingApprovedLeave(
            @Param("orgId") UUID orgId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT lr FROM LeaveRequest lr "
            + "WHERE lr.employee.organization.id = :orgId AND lr.status = 'APPROVED' "
            + "AND lr.startDate >= :start AND lr.startDate <= :end ORDER BY lr.startDate")
    List<LeaveRequest> findUpcomingApprovedByOrganization(
            @Param("orgId") UUID orgId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT lr FROM LeaveRequest lr "
            + "WHERE lr.employee.organization.id = :orgId AND lr.status = 'APPROVED' "
            + "AND lr.endDate >= :weekStart AND lr.startDate <= :weekEnd")
    List<LeaveRequest> findApprovedLeaveOverlappingWeek(
            @Param("orgId") UUID orgId,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);

    @Query("SELECT u.id AS employeeId, "
            + "CONCAT(u.firstName, ' ', u.lastName) AS employeeName, "
            + "u.email AS email, "
            + "COALESCE(SUM(CASE WHEN lr.status = 'APPROVED' THEN lr.totalDays ELSE 0 END), 0) AS totalDaysUsed, "
            + "COALESCE(SUM(CASE WHEN lr.status = 'PENDING' THEN lr.totalDays ELSE 0 END), 0) AS totalDaysPending, "
            + "COALESCE(SUM(CASE WHEN lr.status = 'APPROVED' THEN lr.totalDays ELSE 0 END), 0) AS totalDaysApproved, "
            + "COALESCE(SUM(CASE WHEN lr.status = 'APPROVED' AND lr.startDate >= :yearStart "
            + "AND lr.startDate <= :yearEnd THEN lr.totalDays ELSE 0 END), 0) AS totalDaysThisYear "
            + "FROM User u LEFT JOIN LeaveRequest lr ON lr.employee.id = u.id "
            + "WHERE u.organization.id = :orgId AND u.active = true "
            + "GROUP BY u.id, u.firstName, u.lastName, u.email "
            + "ORDER BY u.lastName, u.firstName")
    List<LeaveBalanceAggregate> aggregateLeaveBalanceByOrganization(
            @Param("orgId") UUID orgId,
            @Param("yearStart") LocalDate yearStart,
            @Param("yearEnd") LocalDate yearEnd);

    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e WHERE e.organization.id = :orgId "
            + "AND (:employeeId IS NULL OR e.id = :employeeId) "
            + "AND (:departmentId IS NULL OR e.department.id = :departmentId) "
            + "AND (:status IS NULL OR lr.status = :status) "
            + "AND (:leaveType IS NULL OR lr.leaveType = :leaveType) "
            + "AND (:startDate IS NULL OR lr.endDate >= :startDate) "
            + "AND (:endDate IS NULL OR lr.startDate <= :endDate)")
    Page<LeaveRequest> findByOrganizationFilters(
            @Param("orgId") UUID orgId,
            @Param("employeeId") UUID employeeId,
            @Param("departmentId") UUID departmentId,
            @Param("status") LeaveRequestStatus status,
            @Param("leaveType") LeaveType leaveType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}

