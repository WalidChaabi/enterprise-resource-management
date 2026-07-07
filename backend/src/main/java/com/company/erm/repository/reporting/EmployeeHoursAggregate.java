package com.company.erm.repository.reporting;

import java.math.BigDecimal;
import java.util.UUID;

public interface EmployeeHoursAggregate {

    UUID getEmployeeId();

    BigDecimal getTotalHours();

    long getEntryCount();
}
