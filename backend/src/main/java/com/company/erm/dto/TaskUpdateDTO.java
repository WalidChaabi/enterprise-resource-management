package com.company.erm.dto;

import com.company.erm.entity.TaskPriority;
import com.company.erm.entity.TaskStatus;
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
}

