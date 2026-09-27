package com.aspire.asat.cms.dto.enums;

import lombok.Getter;

/**
 * Represents types of reminders
 */
@Getter
public enum ReminderType {
    REMINDER_NOT_STARED(100),
    FIRST_REMINDER(50),
    SECOND_REMINDER(20),
    THIRD_REMINDER(10),
    REMINDER_EXPIRY(0);

    private final int percentage;

    ReminderType(int percentage) {
        this.percentage = percentage;
    }

}