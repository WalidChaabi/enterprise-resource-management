package com.company.erm.repository.reporting;

import java.util.UUID;

public interface LeaveBalanceAggregate {

    UUID getEmployeeId();

    String getEmployeeName();

    String getEmail();

    long getTotalDaysUsed();

    long getTotalDaysPending();

    long getTotalDaysApproved();

    long getTotalDaysThisYear();
}
