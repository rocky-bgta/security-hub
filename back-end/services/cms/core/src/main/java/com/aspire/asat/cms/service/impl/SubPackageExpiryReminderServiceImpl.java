package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.dto.enums.ReminderType;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.notification.SubPackageRemainderNotificationData;
import com.aspire.asat.cms.dto.subPackage.ReminderDates;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.service.SubPackageExpiryReminderService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.common.dto.UserDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubPackageExpiryReminderServiceImpl implements SubPackageExpiryReminderService {

    private final UserSubPackageRepository userSubPackageRepository;
    private final CmsNotificationClient notificationClient;
    private final RegistrationServiceClient registrationServiceClient;

    @Value("${subpackage.expiry.reminder.enabled:true}")
    private boolean reminderEnabled;

    @Override
//    @Async will async later
    public void processExpiryReminders(LocalDate checkDate) {
        if (!reminderEnabled) {
            log.debug("Subpackage expiry reminders are disabled");
            return;
        }

        log.info("Processing subpackage expiry reminders for date: {}", checkDate);

        // Find all active subpackages that are not completed and not expired
        List<String> excludeStatuses = List.of(SubPackageStatus.COMPLETED.name());

        List<UserSubPackage> activeSubPackages = userSubPackageRepository
                .findActiveSubPackagesForReminders(excludeStatuses, checkDate);

        log.info("Found {} active subpackages to check for reminders", activeSubPackages.size());

        int remindersSent = 0;
        for (UserSubPackage userSubPackage : activeSubPackages) {
            try {
                ReminderType reminderType = getReminderTypeForDate(userSubPackage, checkDate);

                if (reminderType != null) {
                    log.info("Sending {} reminder for subpackage {} (userId: {})",
                            reminderType, userSubPackage.getSubPackageId(), userSubPackage.getUserId());

                    boolean isSent = sendReminderNotification(userSubPackage, reminderType);

                    // Mark reminder as sent
                    markReminderAsSent(userSubPackage, reminderType,isSent);
                    remindersSent++;
                }
            } catch (Exception e) {
                log.error("Error processing reminder for subpackage {} (userId: {}): {}",
                        userSubPackage.getSubPackageId(), userSubPackage.getUserId(), e.getMessage(), e);
            }
        }

        log.info("Completed processing reminders. Sent {} reminder notifications", remindersSent);
    }

    @Override
    public ReminderDates calculateReminderDates(UserSubPackage userSubPackage) {
        if (userSubPackage.getAssignedDate() == null || userSubPackage.getExpiryDate() == null) {
            log.warn("Cannot calculate reminder dates: assignedDate or expiryDate is null for subpackage {}",
                    userSubPackage.getSubPackageId());
            return null;
        }

        LocalDate assignedDate = userSubPackage.getAssignedDate();
        LocalDate expiryDate = userSubPackage.getExpiryDate();

        // Calculate total days in a validity period
        long totalDays = ChronoUnit.DAYS.between(assignedDate, expiryDate);

        if (totalDays <= 0) {
            log.warn("Invalid validity period: totalDays={} for subpackage {}",
                    totalDays, userSubPackage.getSubPackageId());
            return null;
        }

        // Calculate reminder dates
        // 50% = 50% of a validity period elapsed = 50% remaining
        // 20% = 80% elapsed = 20% remaining  
        // 10% = 90% elapsed = 10% remaining
        LocalDate reminder50Percent = assignedDate.plusDays((long) (totalDays * 0.5));
        LocalDate reminder20Percent = assignedDate.plusDays((long) (totalDays * 0.8));
        LocalDate reminder10Percent = assignedDate.plusDays((long) (totalDays * 0.9));
        LocalDate reminderNotStarted = assignedDate.plusDays((long) (totalDays * 0.2));

        return new ReminderDates(reminderNotStarted, reminder50Percent, reminder20Percent, reminder10Percent, expiryDate);
    }

    @Override
    public ReminderType getReminderTypeForDate(UserSubPackage userSubPackage, LocalDate checkDate) {
        ReminderDates dates = calculateReminderDates(userSubPackage);
        if (dates == null) {
            return null;
        }

        // Check if a reminder should be sent on the check date
        // Check in order: 50%, 20%, 10%, expiry
        // Only check if the reminder hasn't been sent yet

        if (dates.getReminder50Percent() != null &&
                dates.getReminder50Percent().equals(checkDate) &&
                !userSubPackage.isReminder50PercentSent()) {
            return ReminderType.FIRST_REMINDER;
        }

        if (dates.getReminder20Percent() != null &&
                dates.getReminder20Percent().equals(checkDate) &&
                !userSubPackage.isReminder20PercentSent()) {
            return ReminderType.SECOND_REMINDER;
        }

        if (dates.getReminder10Percent() != null &&
                dates.getReminder10Percent().equals(checkDate) &&
                !userSubPackage.isReminder10PercentSent()) {
            return ReminderType.THIRD_REMINDER;
        }

        if (dates.getReminderNotStarted() != null &&
                dates.getReminderNotStarted().equals(checkDate) &&
                !userSubPackage.isReminderNotStartedSent()) {
            return ReminderType.REMINDER_NOT_STARED;
        }

        if (dates.getExpiryDate() != null &&
                dates.getExpiryDate().equals(checkDate) &&
                !userSubPackage.isReminderExpirySent()) {
            return ReminderType.REMINDER_EXPIRY;
        }

        return null;
    }

    @Override
    public boolean sendReminderNotification(UserSubPackage userSubPackage, ReminderType reminderType) {
        // Get email lists based on a reminder type (percentage-wise recipients)
        List<String> additionalEmails = getAdditionalEmailsForReminder(userSubPackage, reminderType);

        // Calculate days remaining
        long daysRemaining = 0;
        if (userSubPackage.getExpiryDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), userSubPackage.getExpiryDate());
        }


        UserDataDto userData = registrationServiceClient.getUserData(userSubPackage.getUserId());
        if (userData == null) {
            log.warn("User data not found for userId: {}, skipping reminder notification", userSubPackage.getUserId());
            return false;
        }

        // Get a username
        String userName = userData.getFirstName() + " " + userData.getLastName();

        if (reminderType.equals(ReminderType.REMINDER_NOT_STARED)) {
            additionalEmails = List.of(userSubPackage.getUserEmail() != null ? 
                    userSubPackage.getUserEmail() : Objects.requireNonNull(userData.getEmail()));
        }

        // Build notification data
        SubPackageRemainderNotificationData notificationData = new SubPackageRemainderNotificationData(
                userSubPackage.getUserEmail() != null ? userSubPackage.getUserEmail() : userData.getEmail(),
                additionalEmails,                // percentage-wise email recipients
                userSubPackage.getUserId(),       // userId for in-app notification
                userSubPackage.getSubPackageName(),
                userSubPackage.getExpiryDate(),
                userSubPackage.getAssignedDate(),  // Include assigned date for accurate calculations
                reminderType.getPercentage(),
                daysRemaining,
                userSubPackage.getClientAdminId(),
                userData.getClientAdminEmail(),
                userName
        );

        // This will send:
        // - REMINDER_NOT_STARED: IN_APP and EMAIL to user (COURSE_NOT_STARTED_USER)
        // - REMINDER_50_PERCENT: IN_APP to user, EMAIL to managers (COURSE_NOT_STARTED_MANAGER)
        // - REMINDER_20_PERCENT: IN_APP to user, EMAIL to C-level (COURSE_NOT_STARTED_C_LEVEL)
        // - REMINDER_10_PERCENT: IN_APP to user, EMAIL to HR (SUBPACKAGE_EXPIRY_REMINDER)
        // - REMINDER_EXPIRY: IN_APP to user, EMAIL to HR (COURSE_EXPIRY_HR)
        boolean sent = notificationClient.sendSubPackageExpiryReminder(notificationData, reminderType);

        if (sent) {
            log.info("Successfully sent {} reminder for subpackage {} - In-app to user: {}, Emails to {} recipients",
                    reminderType, userSubPackage.getSubPackageId(), userSubPackage.getUserId(),
                    additionalEmails != null ? additionalEmails.size() : 0);
        } else {
            log.error("Failed to send {} reminder for subpackage {} to user {}",
                    reminderType, userSubPackage.getSubPackageId(), userSubPackage.getUserId());
        }

        return sent;
    }

    /**
     * Get additional email lists based on a reminder type
     */
    private List<String> getAdditionalEmailsForReminder(UserSubPackage userSubPackage, ReminderType reminderType) {
        return switch (reminderType) {
            case FIRST_REMINDER -> userSubPackage.getSecondaryEmails();
            case SECOND_REMINDER -> userSubPackage.getThirdLevelEmails();
            case THIRD_REMINDER -> userSubPackage.getFourthHREmails();
            case REMINDER_NOT_STARED -> List.of(userSubPackage.getUserEmail());
            case REMINDER_EXPIRY -> {
                // For expiry, combine all email lists
                List<String> allEmails = new ArrayList<>();
                if (userSubPackage.getSecondaryEmails() != null) {
                    allEmails.addAll(userSubPackage.getSecondaryEmails());
                }
                if (userSubPackage.getThirdLevelEmails() != null) {
                    allEmails.addAll(userSubPackage.getThirdLevelEmails());
                }
                if (userSubPackage.getFourthHREmails() != null) {
                    allEmails.addAll(userSubPackage.getFourthHREmails());
                }
                yield allEmails;
            }
        };
    }

    /**
     * Mark reminder as sent in the database
     */
    private void markReminderAsSent(UserSubPackage userSubPackage, ReminderType reminderType, boolean isSent) {
        switch (reminderType) {
            case REMINDER_NOT_STARED -> userSubPackage.setReminderNotStartedSent(isSent);
            case FIRST_REMINDER -> userSubPackage.setReminder50PercentSent(isSent);
            case SECOND_REMINDER -> userSubPackage.setReminder20PercentSent(isSent);
            case THIRD_REMINDER -> userSubPackage.setReminder10PercentSent(isSent);
            case REMINDER_EXPIRY -> userSubPackage.setReminderExpirySent(isSent);
        }

        userSubPackageRepository.save(userSubPackage);
        log.debug("Marked {} reminder as sent for subpackage {}", reminderType, userSubPackage.getSubPackageId());
    }
}

