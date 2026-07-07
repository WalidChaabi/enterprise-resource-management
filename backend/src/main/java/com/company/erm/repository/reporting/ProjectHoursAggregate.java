package com.company.erm.repository.reporting;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProjectHoursAggregate {

    UUID getProjectId();

    BigDecimal getTotalHours();

    long getEntryCount();
}
