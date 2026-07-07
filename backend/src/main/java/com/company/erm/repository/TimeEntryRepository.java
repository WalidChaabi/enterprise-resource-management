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

import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.repository.reporting.EmployeeHoursAggregate;
import com.company.erm.repository.reporting.ProjectHoursAggregate;
import com.company.erm.repository.reporting.ProjectLastActivityAggregate;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByEmployeeId(UUID employeeId);

    List<TimeEntry> findByProjectId(UUID projectId);

    List<TimeEntry> findByTaskId(UUID taskId);

    List<TimeEntry> findByEmployeeIdAndEntryDateBetween(
        UUID employeeId, 
        LocalDate startDate, 
        LocalDate endDate
    );

    List<TimeEntry> findByProjectIdAndEntryDateBetween(
        UUID projectId,
        LocalDate startDate,
        LocalDate endDate
    );

    List<TimeEntry> findByEmployeeIdAndStatus(UUID employeeId, TimeEntryStatus status);

    Optional<TimeEntry> findByIdAndEmployeeOrganizationId(UUID id, UUID organizationId);

    @Query("SELECT te FROM TimeEntry te WHERE te.employee.organization.id = :organizationId")
    List<TimeEntry> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT te FROM TimeEntry te WHERE te.employee.id = :employeeId AND te.entryDate = :entryDate")
    List<TimeEntry> findByEmployeeIdAndEntryDate(@Param("employeeId") UUID employeeId, @Param("entryDate") LocalDate entryDate);

    @Query("SELECT te FROM TimeEntry te WHERE te.employee.organization.id = :orgId AND " +
           "(LOWER(COALESCE(te.description, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(te.project.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(te.task.title, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<TimeEntry> searchByOrganizationIdAndQuery(
            @Param("orgId") UUID orgId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT COUNT(te) FROM TimeEntry te WHERE te.employee.organization.id = :orgId AND " +
           "(LOWER(COALESCE(te.description, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(te.project.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(te.task.title, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    long countSearchByOrganizationIdAndQuery(@Param("orgId") UUID orgId, @Param("query") String query);

    @Query("SELECT COALESCE(SUM(te.durationHours), 0) FROM TimeEntry te "
            + "WHERE te.employee.organization.id = :orgId AND te.status = :status "
            + "AND te.entryDate >= :start AND te.entryDate <= :end")
    java.math.BigDecimal sumHoursByOrganizationStatusAndDateRange(
            @Param("orgId") UUID orgId,
            @Param("status") TimeEntryStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COUNT(te) FROM TimeEntry te "
            + "WHERE te.employee.organization.id = :orgId AND te.status = :status")
    long countByOrganizationIdAndStatus(
            @Param("orgId") UUID orgId,
            @Param("status") TimeEntryStatus status);

    @Query("SELECT te.employee.id AS employeeId, SUM(te.durationHours) AS totalHours, COUNT(te) AS entryCount "
            + "FROM TimeEntry te WHERE te.employee.organization.id = :orgId AND te.status = :status "
            + "AND te.entryDate >= :start AND te.entryDate <= :end GROUP BY te.employee.id")
    List<EmployeeHoursAggregate> sumApprovedHoursByEmployeeInRange(
            @Param("orgId") UUID orgId,
            @Param("status") TimeEntryStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT te.project.id AS projectId, SUM(te.durationHours) AS totalHours, COUNT(te) AS entryCount "
            + "FROM TimeEntry te WHERE te.employee.organization.id = :orgId AND te.status = :status "
            + "AND te.entryDate >= :start AND te.entryDate <= :end GROUP BY te.project.id")
    List<ProjectHoursAggregate> sumApprovedHoursByProjectInRange(
            @Param("orgId") UUID orgId,
            @Param("status") TimeEntryStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COUNT(te) FROM TimeEntry te "
            + "WHERE te.employee.organization.id = :orgId AND te.status = :status "
            + "AND te.entryDate >= :start AND te.entryDate <= :end")
    long countByOrganizationIdAndStatusAndDateRange(
            @Param("orgId") UUID orgId,
            @Param("status") TimeEntryStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT DISTINCT te.project.id FROM TimeEntry te "
            + "WHERE te.employee.organization.id = :orgId AND te.status = 'APPROVED' AND te.entryDate >= :since")
    List<UUID> findProjectIdsWithApprovedEntriesSince(
            @Param("orgId") UUID orgId,
            @Param("since") LocalDate since);

    @Query("SELECT te.project.id AS projectId, MAX(te.entryDate) AS lastEntryDate "
            + "FROM TimeEntry te WHERE te.employee.organization.id = :orgId AND te.status = 'APPROVED' "
            + "GROUP BY te.project.id")
    List<ProjectLastActivityAggregate> findLastApprovedEntryDateByProject(@Param("orgId") UUID orgId);

    @Query("SELECT te FROM TimeEntry te JOIN te.employee e WHERE e.organization.id = :orgId "
            + "AND (:employeeId IS NULL OR e.id = :employeeId) "
            + "AND (:projectId IS NULL OR te.project.id = :projectId) "
            + "AND (:taskId IS NULL OR te.task.id = :taskId) "
            + "AND (:status IS NULL OR te.status = :status) "
            + "AND (:startDate IS NULL OR te.entryDate >= :startDate) "
            + "AND (:endDate IS NULL OR te.entryDate <= :endDate)")
    Page<TimeEntry> findByOrganizationFilters(
            @Param("orgId") UUID orgId,
            @Param("employeeId") UUID employeeId,
            @Param("projectId") UUID projectId,
            @Param("taskId") UUID taskId,
            @Param("status") TimeEntryStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}

