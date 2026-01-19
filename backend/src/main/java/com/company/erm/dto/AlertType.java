package com.company.erm.dto;

/**
 * Types of alerts that can be generated for managers.
 */
public enum AlertType {
    /**
     * Project has no assigned members
     */
    PROJECT_WITHOUT_RESOURCES,
    
    /**
     * Employee is overallocated (working more than capacity)
     */
    EMPLOYEE_OVERALLOCATED,
    
    /**
     * Project is at risk (no time entries in last 7 days for active project)
     */
    PROJECT_AT_RISK,
    
    /**
     * Project is understaffed (required hours > allocated hours)
     */
    PROJECT_UNDERSTAFFED,
    
    /**
     * Project deadline is approaching (within 7 days)
     */
    PROJECT_DEADLINE_APPROACHING,
    
    /**
     * Project is over budget
     */
    PROJECT_OVER_BUDGET
}
