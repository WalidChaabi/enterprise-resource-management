package com.company.erm.webhook;

public enum WebhookEventType {
    LEAVE_APPROVED("leave.approved"),
    TIMEENTRY_SUBMITTED("timeentry.submitted"),
    EMPLOYEE_CREATED("employee.created");

    private final String eventName;

    WebhookEventType(String eventName) {
        this.eventName = eventName;
    }

    public String eventName() {
        return eventName;
    }

    public static WebhookEventType fromEventName(String eventName) {
        for (WebhookEventType type : values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown webhook event: " + eventName);
    }
}
