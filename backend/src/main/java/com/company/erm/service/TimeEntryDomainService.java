package com.company.erm.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.TimeEntryStatus;
import com.company.erm.exception.BusinessException;

@Service
public class TimeEntryDomainService {

    /**
     * Validate time entry creation business rules.
     */
    public void validateTimeEntryCreation(TimeEntry timeEntry) {
        // 1. Entry date cannot be in the future
        if (timeEntry.getEntryDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Entry date cannot be in the future");
        }

        // 2. End time must be after start time (or handle overnight)
        if (timeEntry.getEndTime().isBefore(timeEntry.getStartTime()) 
            && timeEntry.getEndTime().isBefore(LocalTime.of(6, 0))) {
            // Allow overnight entries (e.g., 22:00 to 02:00)
            // But end time should be early morning if it's before start time
        } else if (timeEntry.getEndTime().isBefore(timeEntry.getStartTime()) 
            && timeEntry.getEndTime().isAfter(LocalTime.of(6, 0))) {
            throw new BusinessException("End time must be after start time");
        }

        // 3. Duration must be positive
        if (timeEntry.getDurationHours() == null || timeEntry.getDurationHours().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Duration must be greater than zero");
        }

        // 4. Maximum 24 hours per entry
        if (timeEntry.getDurationHours().compareTo(java.math.BigDecimal.valueOf(24)) > 0) {
            throw new BusinessException("Duration cannot exceed 24 hours per entry");
        }
    }

    /**
     * Validate time entry update business rules.
     */
    public void validateTimeEntryUpdate(TimeEntry timeEntry) {
        // Cannot update approved entries
        if (timeEntry.getStatus() == TimeEntryStatus.APPROVED) {
            throw new BusinessException("Cannot update approved time entry");
        }

        // Re-validate dates and times
        validateTimeEntryCreation(timeEntry);
    }

    /**
     * Validate status transition.
     */
    public void validateStatusTransition(TimeEntryStatus current, TimeEntryStatus next) {
        // Draft -> Submitted, Draft
        // Submitted -> Approved, Rejected, Draft
        // Approved -> (cannot change)
        // Rejected -> Draft, Submitted

        if (current == TimeEntryStatus.APPROVED && next != TimeEntryStatus.APPROVED) {
            throw new BusinessException("Cannot change status of approved time entry");
        }

        if (current == TimeEntryStatus.DRAFT && next == TimeEntryStatus.APPROVED) {
            throw new BusinessException("Cannot approve draft entry. Must submit first.");
        }

        if (current == TimeEntryStatus.REJECTED && next == TimeEntryStatus.APPROVED) {
            throw new BusinessException("Cannot approve rejected entry. Must resubmit first.");
        }
    }

    /**
     * Check if time entry can be deleted.
     */
    public void validateTimeEntryDeletion(TimeEntry timeEntry) {
        if (timeEntry.getStatus() == TimeEntryStatus.APPROVED) {
            throw new BusinessException("Cannot delete approved time entry");
        }
    }

    /**
     * Check for overlapping time entries on the same day.
     */
    public void validateNoOverlap(TimeEntry timeEntry, java.util.List<TimeEntry> existingEntries) {
        for (TimeEntry existing : existingEntries) {
            if (existing.getId().equals(timeEntry.getId())) {
                continue; // Skip self
            }

            // Check if same date
            if (!existing.getEntryDate().equals(timeEntry.getEntryDate())) {
                continue;
            }

            // Check for time overlap
            LocalTime existingStart = existing.getStartTime();
            LocalTime existingEnd = existing.getEndTime();
            LocalTime newStart = timeEntry.getStartTime();
            LocalTime newEnd = timeEntry.getEndTime();

            // Simple overlap check (doesn't handle overnight entries perfectly)
            boolean overlaps = (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart));
            
            if (overlaps) {
                throw new BusinessException(
                    String.format("Time entry overlaps with existing entry on %s (%s - %s)",
                        existing.getEntryDate(),
                        existingStart,
                        existingEnd)
                );
            }
        }
    }
}

