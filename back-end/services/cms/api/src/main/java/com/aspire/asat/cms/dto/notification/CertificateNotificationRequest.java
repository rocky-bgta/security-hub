package com.aspire.asat.cms.dto.notification;

public record CertificateNotificationRequest(
        String userEmail,
        String userId,
        String userName,
        String courseTitle,
        String issueDate,
        String adminEmail,
        String adminName,
        String clientAdminId) {
}
