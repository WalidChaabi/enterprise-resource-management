package com.company.erm.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class TimeEntryUpdateDTO {

    private Optional<UUID> taskId = Optional.empty();

    private Optional<LocalDate> entryDate = Optional.empty();

    private Optional<LocalTime> startTime = Optional.empty();

    private Optional<LocalTime> endTime = Optional.empty();

    private Optional<String> description = Optional.empty();

    public TimeEntryUpdateDTO() {
        // Default constructor
    }

    public boolean hasUpdates() {
        return taskId.isPresent() || entryDate.isPresent() || startTime.isPresent()
            || endTime.isPresent() || description.isPresent();
    }

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isHasUpdates() {
        return hasUpdates();
    }

    @AssertTrue(message = "Description must not exceed 5000 characters")
    public boolean isDescriptionLengthValid() {
        return description.isEmpty() || description.get().length() <= 5000;
    }
}
