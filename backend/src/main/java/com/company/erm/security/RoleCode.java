package com.company.erm.security;

/**
 * Canonical role codes stored in {@code roles.code} and used in Spring Security expressions.
 */
public enum RoleCode {
    ORG_ADMIN,
    HR_ADMIN,
    PROJECT_MANAGER,
    DEPARTMENT_MANAGER,
    EMPLOYEE;

    public String code() {
        return name();
    }
}
