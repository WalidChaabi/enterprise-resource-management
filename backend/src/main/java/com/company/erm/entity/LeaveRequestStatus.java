package com.company.erm.entity;

public enum LeaveRequestStatus {
    PENDING,    // Request submitted, awaiting approval
    APPROVED,   // Request approved by manager
    REJECTED,   // Request rejected by manager
    CANCELLED   // Request cancelled by employee
}

