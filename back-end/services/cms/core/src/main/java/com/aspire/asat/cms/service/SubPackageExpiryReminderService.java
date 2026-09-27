package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.enums.ReminderType;
import com.aspire.asat.cms.dto.subPackage.ReminderDates;
import com.aspire.asat.cms.model.UserSubPackage;

import java.time.LocalDate;

/**
 * Service for handling subpackage expiry reminder logic
 */
public interface SubPackageExpiryReminderService {
    
    /**
     * Process expiry reminders for subpackages that need notifications
     * @param checkDate the date to check for reminders (typically today)
     */
    void processExpiryReminders(LocalDate checkDate);
    
    /**
     * Calculate reminder dates for a subpackage
     * @param userSubPackage the subpackage to calculate reminders for
     * @return ReminderDates object containing all reminder dates
     */
    ReminderDates calculateReminderDates(UserSubPackage userSubPackage);
    
    /**
     * Check if a reminder should be sent for a subpackage on the given date
     * @param userSubPackage the subpackage to check
     * @param checkDate the date to check
     * @return the reminder type that should be sent or null if no reminder needed
     */
    ReminderType getReminderTypeForDate(UserSubPackage userSubPackage, LocalDate checkDate);
    
    /**
     * Send reminder notification for a subpackage
     * @param userSubPackage the subpackage to send a reminder for
     * @param reminderType the type of reminder to send
     */
    boolean sendReminderNotification(UserSubPackage userSubPackage, ReminderType reminderType);
}

