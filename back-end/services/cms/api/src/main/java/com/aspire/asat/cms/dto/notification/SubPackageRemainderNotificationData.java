package com.aspire.asat.cms.dto.notification;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SubPackageRemainderNotificationData(
        String userEmail,
        List<String> additionalEmails,
        String userId,
        String subPackageName,
        LocalDate expiryDate,
        LocalDate assignedDate,  // Added to calculate days since assignment
        int reminderPercentage,
        long daysRemaining,
        String clientAdminId,
        String clientAdminEmail,
        String clientAdminName

) {
}