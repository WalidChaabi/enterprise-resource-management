package com.company.erm.repository.reporting;

import java.time.LocalDate;
import java.util.UUID;

public interface ProjectLastActivityAggregate {

    UUID getProjectId();

    LocalDate getLastEntryDate();
}
