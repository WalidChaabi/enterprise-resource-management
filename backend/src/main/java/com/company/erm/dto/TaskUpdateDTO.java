package com.company.erm.dto;

import com.company.erm.entity.TaskPriority;
import com.company.erm.entity.TaskStatus;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class TaskUpdateDTO {

    private Optional<String> title = Optional.empty();

    private Optional<String> description = Optional.empty();

    private Optional<TaskStatus> status = Optional.empty();

    private Optional<TaskPriority> priority = Optional.empty();

    private Optional<LocalDate> dueDate = Optional.empty();

    private Optional<UUID> assignedToId = Optional.empty();

    public TaskUpdateDTO() {
        // Default constructor
    }

    public boolean hasUpdates() {
        return title.isPresent() || description.isPresent() || status.isPresent()
            || priority.isPresent() || dueDate.isPresent() || assignedToId.isPresent();
    }

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isHasUpdates() {
        return hasUpdates();
    }

    @AssertTrue(message = "Title must not be blank when provided")
    public boolean isTitlePresentAndValid() {
        return title.isEmpty() || !title.get().isBlank();
    }

    @AssertTrue(message = "Title must not exceed 255 characters")
    public boolean isTitleLengthValid() {
        return title.isEmpty() || title.get().length() <= 255;
    }

    @AssertTrue(message = "Description must not exceed 5000 characters")
    public boolean isDescriptionLengthValid() {
        return description.isEmpty() || description.get().length() <= 5000;
    }
}
