package com.aspire.asat.cms.scheduler;

import com.aspire.asat.cms.service.SubPackageExpiryReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduler for sending subpackage expiry reminder notifications
 * Runs periodically to check for subpackages that need reminder notifications
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubPackageExpiryReminderScheduler {

    private final SubPackageExpiryReminderService reminderService;

    @Value("${subpackage.expiry.reminder.enabled:true}")
    private boolean reminderEnabled;

    /**
     * Scheduled task to check and send subpackage expiry reminders
     * Runs every day at midnight (00:00:00) by default (configurable via properties)
     * Cron format: second minute hour day month weekday
     * Default: "0 0 0 * * ?" means every day at midnight
     */
    @Scheduled(cron = "${subpackage.expiry.reminder.scheduler.cron:0 0 0 * * ?}")
    public void checkAndSendExpiryReminders() {
        if (!reminderEnabled) {
            log.debug("Subpackage expiry reminder scheduler is disabled");
            return;
        }

        try {
            LocalDate today = LocalDate.now();
            log.debug("Running subpackage expiry reminder check for date: {}", today);

            reminderService.processExpiryReminders(today);

        } catch (Exception e) {
            log.error("Error in subpackage expiry reminder scheduler: {}", e.getMessage(), e);
        }
    }
}

