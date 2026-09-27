package com.aspire.asat.common.enums.notification;

import lombok.Getter;

/**
 * Priority levels for notifications
 */
@Getter
public enum NotificationPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4);

    private final int level;

    NotificationPriority(int level) {
        this.level = level;
    }

}