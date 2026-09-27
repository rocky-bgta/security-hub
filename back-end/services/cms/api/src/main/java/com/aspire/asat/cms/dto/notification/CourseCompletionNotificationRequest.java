package com.aspire.asat.cms.dto.notification;

public record CourseCompletionNotificationRequest(
        String userEmail,
        String userId,
        String userName,
        String courseTitle,
        String completionDate,
        String completionTimestamp,
        String adminEmail,
        String adminName,
        String clientAdminId) {
}
