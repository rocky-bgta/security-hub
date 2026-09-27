package com.aspire.asat.cms.dto.enums;

public enum TopicStatus {
    ENABLED,
    DISABLED,
    PENDING, // only use for the front end
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    public static TopicStatus fromString(String status) {
        if (status == null) return null;
        String normalized = status.trim();
        try {
            return TopicStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    public static TopicStatus getCompletedOrPendingStatus(String status) {
        return status.equalsIgnoreCase(COMPLETED.name()) ? COMPLETED : PENDING;
    }

}
