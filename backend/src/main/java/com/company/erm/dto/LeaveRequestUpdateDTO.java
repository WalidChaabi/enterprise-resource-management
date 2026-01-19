package com.company.erm.dto;

import com.company.erm.entity.LeaveType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Setter
public class LeaveRequestUpdateDTO {

    private Optional<LeaveType> leaveType = Optional.empty();

    private Optional<LocalDate> startDate = Optional.empty();

    private Optional<LocalDate> endDate = Optional.empty();

    private Optional<String> reason = Optional.empty();

    public LeaveRequestUpdateDTO() {
        // Default constructor
    }

    public boolean hasUpdates() {
        return leaveType.isPresent() || startDate.isPresent() 
            || endDate.isPresent() || reason.isPresent();
    }
}

