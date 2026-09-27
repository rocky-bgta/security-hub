package com.aspire.asat.notification.controller.impl;

import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.notification.controller.NotificationController;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.dto.dispatch.NotificationDispatchResultDto;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.service.NotificationDeliveryService;
import com.aspire.asat.notification.service.NotificationDispatchService;
import com.aspire.asat.notification.service.NotificationEscalationService;
import com.aspire.asat.notification.service.NotificationHistoryService;
import com.aspire.asat.notification.service.NotificationProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Legacy notification controller implementation for email notifications
 * For new notification features, use NotificationManagementController
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationControllerImpl implements NotificationController {

    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationProducerService notificationProducerService;
    private final NotificationHistoryService notificationHistoryService;
    private final NotificationDispatchService notificationDispatchService;
    private final NotificationEscalationService notificationEscalationService;

  public ResponseEntity<ApiResponseDto<EmailDto>> sendEmail(@RequestBody EmailDto emailDto) {
        log.info("Sending legacy email notification to: {}", emailDto.getTo());
        
        EmailDto queuedEmail = notificationProducerService.queueEmailNotification(emailDto);
        
        ApiResponseDto<EmailDto> response = new ApiResponseDto<>(
            "Email queued successfully", 200, queuedEmail);
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<ApiResponseDto<NotificationRequestDto>> sendNotification(@RequestBody NotificationRequestDto requestDto) {
        // Generate logId in the parent method (controller)
        String logId = UUID.randomUUID().toString();
        
        try {
            log.info("Sending notification: type={}, channels={}, to={}, logId={}",
                    requestDto.getNotificationType(), requestDto.getChannels(), requestDto.getTo(), logId);

            // Validate request
            validateNotificationRequest(requestDto);

            // Create notification log entry asynchronously (non-blocking)
            notificationHistoryService.createNotificationLog(requestDto, logId);

            // Deliver notification (this will update the log via NotificationDeliveryService)
            notificationDeliveryService.deliverNotification(requestDto, logId);

            // Finalize log entry asynchronously (non-blocking)
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.SUCCESS);

            // Legacy bridge: fan out to MSP / Aspire Admin in the background when the role
            // matrix allows it. Never affects the primary delivery result (see class javadoc).
            notificationEscalationService.escalate(requestDto, logId);

            ApiResponseDto<NotificationRequestDto> response = new ApiResponseDto<>(
                    "Notification sent successfully", HttpStatus.OK.value(), requestDto);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid notification request: {}", e.getMessage());
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.FAILED);
            ApiResponseDto<NotificationRequestDto> errorResponse = new ApiResponseDto<>(
                    "Invalid request: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(), null);
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.FAILED);
            ApiResponseDto<NotificationRequestDto> errorResponse = new ApiResponseDto<>(
                    "Failed to send notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public ResponseEntity<ApiResponseDto<NotificationDispatchResultDto>> sendRoleBasedNotification(
            @Valid @RequestBody NotificationEventRequestDto eventRequestDto) {
        log.info("Received role-based notification event: type={}, targetUserId={}",
                eventRequestDto.getNotificationType(), eventRequestDto.getTargetUserId());

        try {
            NotificationDispatchResultDto result = notificationDispatchService.dispatch(eventRequestDto);
            ApiResponseDto<NotificationDispatchResultDto> response = new ApiResponseDto<>(
                    "Notification dispatched to " + result.getSentCount() + " recipient(s)",
                    HttpStatus.OK.value(), result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to dispatch role-based notification: {}", e.getMessage(), e);
            ApiResponseDto<NotificationDispatchResultDto> errorResponse = new ApiResponseDto<>(
                    "Failed to dispatch notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Validate notification request
     */
    private void validateNotificationRequest(NotificationRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        
        if (requestDto.getTo() == null || requestDto.getTo().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient 'to' field cannot be empty");
        }
        
        if (requestDto.getNotificationType() == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        
        if (requestDto.getChannels() == null || requestDto.getChannels().isEmpty()) {
            throw new IllegalArgumentException("At least one notification channel is required");
        }
        
        // Validate email format if an EMAIL channel is used
        if (requestDto.getChannels().contains(NotificationChannel.EMAIL)) {
            if (!isValidEmail(requestDto.getTo())) {
                throw new IllegalArgumentException("Invalid email format: " + requestDto.getTo());
            }
        }
        
        // Validate userId for IN_APP channel
        if (requestDto.getChannels().contains(NotificationChannel.IN_APP)) {
            if (requestDto.getUserId() == null || requestDto.getUserId().trim().isEmpty()) {
                throw new IllegalArgumentException("User ID is required for in-app notifications");
            }
        }
    }

    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

}