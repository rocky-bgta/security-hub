package com.aspire.asat.registration.service.external;

import com.aspire.asat.registration.data.notification.NotificationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service responsible for handling notification-related external service calls for MSP onboarding.
 * This service encapsulates all interactions with the notification service for MSP-specific notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final WebClient webClient;

    @Value("${client.notification.url}")
    private String notificationServiceUrl;

    /**
     * Sends a welcome email notification to the MSP admin after successful onboarding.
     *
     * @param adminEmail The email address of the MSP admin
     * @param organizationName The name of the MSP organization
     * @param tempPassword The temporary password for the MSP admin
     */
    @Async
    public void sendMspOnboardingWelcomeEmail(String adminEmail, String organizationName, String tempPassword) {
        log.info("Sending MSP onboarding welcome email to: {}", adminEmail);
        
        NotificationRequestDto requestDto = buildWelcomeEmailRequest(adminEmail, organizationName, tempPassword);
        
        webClient.post()
                .uri(notificationServiceUrl + "/api/v1/send")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestDto), NotificationRequestDto.class)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Successfully sent MSP onboarding welcome email to: {} with status: {}", 
                        adminEmail, response.getStatusCode()))
                .doOnError(error -> log.error("Failed to send MSP onboarding welcome email to: {}", adminEmail, error))
                .subscribe();
    }

    /**
     * Sends an invoice notification email to the MSP admin.
     *
     * @param adminEmail The email address of the MSP admin
     * @param organizationName The name of the MSP organization
     * @param invoiceId The ID of the created invoice
     */
    @Async
    public void sendInvoiceNotificationEmailForMsp(String adminEmail, String organizationName, String invoiceId) {
        log.info("Sending invoice notification email to: {} for invoice: {}", adminEmail, invoiceId);
        
        NotificationRequestDto requestDto = buildInvoiceNotificationRequest(adminEmail, organizationName, invoiceId);
        
        webClient.post()
                .uri(notificationServiceUrl + "/api/v1/send")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestDto), NotificationRequestDto.class)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Successfully sent invoice notification email to: {} with status: {}", 
                        adminEmail, response.getStatusCode()))
                .doOnError(error -> log.error("Failed to send invoice notification email to: {}", adminEmail, error))
                .subscribe();
    }

    /**
     * Builds the welcome email notification request for MSP onboarding.
     *
     * @param adminEmail The admin email
     * @param organizationName The organization name
     * @param tempPassword The temporary password
     * @return The notification request DTO
     */
    private NotificationRequestDto buildWelcomeEmailRequest(String adminEmail, String organizationName, String tempPassword) {
        return NotificationRequestDto.builder()
                .to(adminEmail)
                .subject("Welcome to Aspire MSP Platform")
                .templateId("msp-welcome-email")
                .templateModel(Map.of(
                        "organizationName", organizationName,
                        "adminEmail", adminEmail,
                        "tempPassword", tempPassword
                ))
                .build();
    }

    /**
     * Builds the invoice notification request for MSP onboarding.
     *
     * @param adminEmail The admin email
     * @param organizationName The organization name
     * @param invoiceId The invoice ID
     * @return The notification request DTO
     */
    private NotificationRequestDto buildInvoiceNotificationRequest(String adminEmail, String organizationName, String invoiceId) {
        return NotificationRequestDto.builder()
                .to(adminEmail)
                .subject("Invoice Generated - MSP Onboarding")
                .templateId("msp-invoice-notification")
                .templateModel(Map.of(
                        "organizationName", organizationName,
                        "invoiceId", invoiceId
                ))
                .build();
    }
}
