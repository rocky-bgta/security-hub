package com.aspire.asat.phishing.client;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Phishing-service-specific notification client.
 * Handles notifications related to phishing operations such as domain verification.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PhishingNotificationClient {

    private final NotificationClient notificationClient;

    /**
     * Send domain verification email with the 6-digit verification code.
     *
     * @param email            recipient email address
     * @param verificationCode 6-digit verification code
     * @param domainName       domain being verified
     */
    public void sendDomainVerificationEmail(String email, String verificationCode, String domainName) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("verificationCode", verificationCode);
            templateModel.put("domainName", domainName);
            templateModel.put("email", email);
            templateModel.put("expiryTime", "15 minutes");

            log.info("Sending domain verification email to {} for domain {}", email, domainName);

            boolean sent = notificationClient.sendEmailNotification(
                    email, NotificationType.DOMAIN_VERIFICATION, templateModel);

            if (sent) {
                log.info("Domain verification email sent successfully to {}", email);
            } else {
                log.warn("Domain verification email may not have been delivered to {}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send domain verification email to {}: {}", email, e.getMessage(), e);
        }
    }
}
