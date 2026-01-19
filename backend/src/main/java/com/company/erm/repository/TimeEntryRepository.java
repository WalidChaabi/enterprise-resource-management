package com.company.erm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;

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
}

