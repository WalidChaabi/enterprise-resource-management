package com.company.erm.entity;

public enum TimeEntryStatus {
    DRAFT,      // Entry is being created/edited
    SUBMITTED,  // Entry submitted for approval
    APPROVED,   // Entry approved by manager
    REJECTED   // Entry rejected by manager
}

