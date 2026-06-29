package com.company.erm.entitlement;

import java.util.Locale;

public enum PlanCode {
    TRIAL,
    STARTER,
    PRO,
    FREE;

    public static PlanCode from(String value) {
        if (value == null || value.isBlank()) {
            return FREE;
        }
        try {
            return PlanCode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return FREE;
        }
    }
}
