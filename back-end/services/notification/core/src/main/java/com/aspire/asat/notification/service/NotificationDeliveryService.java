package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.InAppNotificationDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.notification.config.NotificationBrandingProperties;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.enums.ChannelStatus;
import com.aspire.asat.notification.model.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced service for delivering notifications through multiple channels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryService {
    
    private final NotificationPreferenceResolver preferenceResolver;
    private final NotificationTemplateService templateService;
    private final NotificationProducerService producerService;
    private final InAppNotificationService inAppService;
    private final NotificationBrandingProperties brandingProperties;
    private final NotificationHistoryService notificationHistoryService;
    private final SmsService smsService;
    private final PhoneCallService phoneCallService;
    
    /**
     * Deliver notification through requested channels
     */
    public void deliverNotification(NotificationRequestDto request) {
        deliverNotification(request, null);
    }
    
    /**
     * Deliver notification through requested channels with logging
     */
    public void deliverNotification(NotificationRequestDto request, String logId) {
        log.info("Processing notification request: type={}, channels={}, to={}", 
            request.getNotificationType(), request.getChannels(), request.getTo());
        
        try {
            // Step 1: A validate notification type is enabled
            // Notification Preference Management (4-layer gate chain):
            // 1. Global Notification Settings - must be enabled
            // 2. Role-based settings (Aspire Admin, MSP, Client Admin, User) - only if request carries a recipientRole
            // 3. Client-wise (organization) Notification Settings - only if it exists
            // 4. User-level Notification Settings - only if it exists
            String clientAdminId = request.getClientAdminId();
            boolean isEnabled = preferenceResolver.isTypeAllowed(
                    request.getNotificationType(), request.getRecipientRole(), clientAdminId, request.getUserId());
            
            if (!isEnabled) {
                log.warn("Notification type {} is disabled for client {}, skipping delivery", 
                        request.getNotificationType(), clientAdminId != null ? clientAdminId : "global");
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, null, ChannelStatus.SKIPPED, 
                            "Notification type is disabled");
                }
                throw new IllegalArgumentException("Notification type " + request.getNotificationType() + " is disabled");
            }
            
            // Step 2: Process each requested channel
            // Notification Preference Management:
            // 1. First, check Global channel settings - must be enabled
            // 2. Then check Client-wise channel settings - only if it exists
            // 3. If a client setting exists, use client preference; if not, skip and use global
            for (NotificationChannel channel : request.getChannels()) {
                boolean isChannelEnabled = preferenceResolver.isChannelAllowed(
                        request.getNotificationType(), channel, request.getRecipientRole(), clientAdminId, request.getUserId());
                
                if (!isChannelEnabled) {
                    log.warn("Channel {} is disabled for notification type {} (client: {}), skipping", 
                            channel, request.getNotificationType(), clientAdminId != null ? clientAdminId : "global");
                    if (logId != null) {
                        notificationHistoryService.updateChannelStatus(logId, channel, ChannelStatus.SKIPPED, 
                                "Channel is disabled");
                    }
                    continue; // Skip this channel but continue with others
                }
                
                deliverToChannel(request, channel, logId);
            }
            
            log.info("Successfully delivered notification: type={}, channels={}, to={}",
                request.getNotificationType(), request.getChannels(), request.getTo());
                
        } catch (Exception e) {
            log.error("Failed to deliver notification: type={}, channels={}, to={}, error={}",
                request.getNotificationType(), request.getChannels(), request.getTo(), e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, null, ChannelStatus.FAILED, e.getMessage());
            }
            throw e; // Re-throw to be handled by controller
        }
    }

    /**
     * Deliver notification to a specific channel with logging
     */
    private void deliverToChannel(NotificationRequestDto request, NotificationChannel channel, String logId) {
        try {
            switch (channel) {
                case EMAIL:
                    deliverEmailNotification(request, logId);
                    break;
                case IN_APP:
                    deliverInAppNotification(request, logId);
                    break;
                case SMS:
                    deliverSmsNotification(request, logId);
                    break;
                case PHONE_CALL:
                    deliverPhoneCallNotification(request, logId);
                    break;
                case PUSH:
                    deliverPushNotification(request, logId);
                    break;
                default:
                    log.warn("Unsupported notification channel: {}", channel);
                    if (logId != null) {
                        notificationHistoryService.updateChannelStatus(logId, channel, ChannelStatus.SKIPPED, 
                                "Unsupported channel");
                    }
            }
        } catch (Exception e) {
            log.error("Failed to deliver notification via {}: {}", channel, e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, channel, ChannelStatus.FAILED, e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Deliver email notification
     */
    private void deliverEmailNotification(NotificationRequestDto request) {
        deliverEmailNotification(request, null);
    }
    
    /**
     * Deliver email notification with logging
     */
    private void deliverEmailNotification(NotificationRequestDto request, String logId) {
        log.info("Delivering email notification for type: {}", request.getNotificationType());
        
        try {
            // Get template for email channel (role-specific first, falling back to role-agnostic)
            Optional<NotificationTemplate> templateOpt = templateService.getTemplate(
                request.getNotificationType(), NotificationChannel.EMAIL, request.getRecipientRole());
            
            if (templateOpt.isEmpty()) {
                log.error("No email template found for notification type: {}", request.getNotificationType());
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.EMAIL, 
                            ChannelStatus.FAILED, "No email template found");
                }
                throw new IllegalArgumentException("No email template found for notification type: " + request.getNotificationType());
            }
            
            NotificationTemplate template = templateOpt.get();
            
            // Merge template model with branding properties
            Map<String, Object> enrichedModel = enrichTemplateModel(request.getTemplateModel());
            
            // Generate content
            String subject = templateService.generateSubject(template, enrichedModel);

            // Create email DTO
            EmailDto emailDto = new EmailDto();
            emailDto.setTo(request.getTo());
            emailDto.setSubject(subject);
            emailDto.setTemplateId(template.getId());
            emailDto.setTemplateModel(enrichedModel);
            emailDto.setAttachments(request.getAttachments());
            
            // Store logId in metadata for later retrieval in ConsumerService
            if (logId != null && emailDto.getMetadata() == null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("notificationLogId", logId);
                emailDto.setMetadata(metadata);
            } else if (logId != null) {
                emailDto.getMetadata().put("notificationLogId", logId);
            }
            
            // Send email via an existing producer service
            producerService.queueEmailNotification(emailDto);
            
            // Note: Email status will be updated in ConsumerService after actual send
            log.info("Email notification queued successfully for {}", request.getTo());
            
        } catch (Exception e) {
            log.error("Failed to deliver email notification: type={}, to={}, error={}", 
                request.getNotificationType(), request.getTo(), e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, NotificationChannel.EMAIL, 
                        ChannelStatus.FAILED, e.getMessage());
            }
            throw new AspireException("Failed to deliver email notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deliver in-app notification
     */
    private void deliverInAppNotification(NotificationRequestDto request) {
        deliverInAppNotification(request, null);
    }
    
    /**
     * Deliver in-app notification with logging
     */
    private void deliverInAppNotification(NotificationRequestDto request, String logId) {
        log.info("Delivering in-app notification for type: {}", request.getNotificationType());
        
        try {
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.error("User ID is required for in-app notifications");
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.IN_APP, 
                            ChannelStatus.FAILED, "User ID is required");
                }
                throw new IllegalArgumentException("User ID is required for in-app notifications");
            }
            
            // Merge template model with branding properties
            Map<String, Object> enrichedModel = enrichTemplateModel(request.getTemplateModel());
            
            // Get a template for an in-app channel (role-specific first, falling back to role-agnostic)
            Optional<NotificationTemplate> templateOpt = templateService.getTemplate(
                request.getNotificationType(), NotificationChannel.IN_APP, request.getRecipientRole());
            
            String title;
            String message;
            
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                title = templateService.generateTitle(template, enrichedModel);
                message = templateService.generateMessage(template, enrichedModel);
            } else {
                // Fallback to basic content
                title = generateDefaultTitle(request.getNotificationType());
                message = generateDefaultMessage(request.getNotificationType(), enrichedModel);
            }
            
            // Create in-app notification DTO
            InAppNotificationDto inAppDto = InAppNotificationDto.builder()
                .userId(request.getUserId())
                .title(title)
                .message(message)
                .notificationType(request.getNotificationType())
                .metadata(request.getMetadata())
                .build();
            
            // Store logId in metadata for later retrieval in InAppNotificationService
            if (logId != null && inAppDto.getMetadata() == null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("notificationLogId", logId);
                inAppDto.setMetadata(metadata);
            } else if (logId != null) {
                inAppDto.getMetadata().put("notificationLogId", logId);
            }
            
            // Send in-app notification
            inAppService.createNotification(inAppDto, logId);
            
            log.info("In-app notification created successfully for user {}", request.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to deliver in-app notification: type={}, userId={}, error={}", 
                request.getNotificationType(), request.getUserId(), e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, NotificationChannel.IN_APP, 
                        ChannelStatus.FAILED, e.getMessage());
            }
            throw new AspireException("Failed to deliver in-app notification: " + e.getMessage(), e);
        }
    }

    
    private void deliverSmsNotification(NotificationRequestDto request, String logId) {
        log.info("Delivering SMS notification for type: {}", request.getNotificationType());
        
        try {
            // Extract phone number from request
            String phoneNumber = request.getPhoneNumber();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                // Fallback to 'to' field if phoneNumber is not provided
                phoneNumber = request.getTo();
            }
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                log.error("Phone number is required for SMS notification");
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.SMS, 
                            ChannelStatus.FAILED, "Phone number is required");
                }
                throw new IllegalArgumentException("Phone number is required for SMS notification");
            }
            
            // Merge template model with branding properties
            Map<String, Object> enrichedModel = enrichTemplateModel(request.getTemplateModel());
            
            // Get SMS template if available (role-specific first, falling back to role-agnostic)
            Optional<NotificationTemplate> templateOpt = templateService.getTemplate(
                request.getNotificationType(), NotificationChannel.SMS, request.getRecipientRole());
            
            String message;
            if (templateOpt.isPresent() && templateOpt.get().getTextTemplate() != null
                    && !templateOpt.get().getTextTemplate().isBlank()) {
                NotificationTemplate template = templateOpt.get();
                message = templateService.generateTextContent(template, enrichedModel);
            } else {
                // Fallback: generate message from template model
                // For MFA OTP, extract OTP from templateModel
                Object otpObj = enrichedModel.get("otp");
                if (otpObj != null) {
                    message = "Your verification code is: " + otpObj.toString();
                } else {
                    message = generateDefaultMessage(request.getNotificationType(), enrichedModel);
                }
            }
            
            // Send SMS via SmsService
            boolean sent = smsService.sendSms(phoneNumber, message);
            
            if (sent) {
                log.info("SMS notification sent successfully to: {}", phoneNumber);
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.SMS, 
                            ChannelStatus.SUCCESS, "SMS sent successfully");
                }
            } else {
                log.error("Failed to send SMS notification to: {}", phoneNumber);
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.SMS, 
                            ChannelStatus.FAILED, "Failed to send SMS");
                }
                throw new AspireException("Failed to send SMS notification");
            }
            
        } catch (Exception e) {
            log.error("Failed to deliver SMS notification: type={}, to={}, error={}", 
                request.getNotificationType(), request.getTo(), e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, NotificationChannel.SMS, 
                        ChannelStatus.FAILED, e.getMessage());
            }
            throw new AspireException("Failed to deliver SMS notification: " + e.getMessage(), e);
        }
    }

    
    private void deliverPhoneCallNotification(NotificationRequestDto request, String logId) {
        log.info("Delivering Phone Call notification for type: {}", request.getNotificationType());
        
        try {
            // Extract phone number from request
            String phoneNumber = request.getPhoneNumber();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                // Fallback to 'to' field if phoneNumber is not provided
                phoneNumber = request.getTo();
            }
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                log.error("Phone number is required for Phone Call notification");
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PHONE_CALL, 
                            ChannelStatus.FAILED, "Phone number is required");
                }
                throw new IllegalArgumentException("Phone number is required for Phone Call notification");
            }
            
            // Extract OTP code from template model
            Map<String, Object> templateModel = request.getTemplateModel();
            if (templateModel == null) {
                log.error("Template model is required for Phone Call notification (must contain OTP code)");
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PHONE_CALL, 
                            ChannelStatus.FAILED, "Template model with OTP is required");
                }
                throw new IllegalArgumentException("Template model with OTP code is required for Phone Call notification");
            }
            
            Object otpObj = templateModel.get("otp");
            if (otpObj == null) {
                log.error("OTP code is required in template model for Phone Call notification");
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PHONE_CALL, 
                            ChannelStatus.FAILED, "OTP code is required");
                }
                throw new IllegalArgumentException("OTP code is required in template model for Phone Call notification");
            }
            
            String otpCode = otpObj.toString();
            
            // Make phone call via PhoneCallService
            boolean sent = phoneCallService.makeCall(phoneNumber, otpCode);
            
            if (sent) {
                log.info("Phone Call notification initiated successfully to: {}", phoneNumber);
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PHONE_CALL, 
                            ChannelStatus.SUCCESS, "Phone call initiated successfully");
                }
            } else {
                log.error("Failed to initiate Phone Call notification to: {}", phoneNumber);
                if (logId != null) {
                    notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PHONE_CALL, 
                            ChannelStatus.FAILED, "Failed to initiate phone call");
                }
                throw new AspireException("Failed to initiate Phone Call notification");
            }
            
        } catch (Exception e) {
            log.error("Failed to deliver Phone Call notification: type={}, to={}, error={}", 
                request.getNotificationType(), request.getTo(), e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PHONE_CALL, 
                        ChannelStatus.FAILED, e.getMessage());
            }
            throw new AspireException("Failed to deliver Phone Call notification: " + e.getMessage(), e);
        }
    }

    
    private void deliverPushNotification(NotificationRequestDto request, String logId) {
        log.info("Push notification delivery not yet implemented for type: {}", request.getNotificationType());
        if (logId != null) {
            notificationHistoryService.updateChannelStatus(logId, NotificationChannel.PUSH, 
                    ChannelStatus.SKIPPED, "Push not yet implemented");
        }
    }
    
    /**
     * Generate the default title for a notification type
     */
    private String generateDefaultTitle(NotificationType notificationType) {
        return switch (notificationType) {
            case NEW_USER_REGISTERED -> "Welcome to ASAT Platform";
            case USER_SUSPENDED -> "Account Suspended";
            case USER_SUSPENSION -> "Account Suspended";
            case USER_ACTIVATED -> "Account Reactivated";
            case COURSE_ASSIGNED -> "New Course Assignment";
            case COURSE_COMPLETION -> "Course Completed";
            case COURSE_COMPLETION_USER -> "Course Completed";
            case PACKAGE_ASSIGNED -> "Course Assigned";
            case CERTIFICATE_ISSUED -> "Certificate Issued";
            default -> notificationType.getDisplayName();
        };
    }
    
    /**
     * Generate a default message for a notification type
     */
    private String generateDefaultMessage(NotificationType notificationType, Map<String, Object> templateModel) {
        String userName = templateModel != null ? (String) templateModel.get("userName") : "User";
        String courseTitle = templateModel != null ? (String) templateModel.get("courseTitle") : "your course";

        return switch (notificationType) {
            case NEW_USER_REGISTERED -> "Welcome " + userName + "! Your account has been created successfully.";
            case USER_SUSPENDED -> "Your account has been suspended. Please contact support for assistance.";
            case USER_SUSPENSION -> "Your account has been suspended. Please contact support for assistance.";
            case COURSE_ASSIGNED -> "A new course has been assigned to you. Please check your dashboard.";
            case COURSE_COMPLETION -> userName + " has completed the course '" + courseTitle + "'.";
            case COURSE_COMPLETION_USER -> "Congratulations! You have completed the course '" + courseTitle + "'.";
            case PACKAGE_ASSIGNED -> "A new course has been assigned to you.";
            case CERTIFICATE_ISSUED -> "Congratulations! You have earned a certificate.";
            default -> "You have a new " + notificationType.getDisplayName().toLowerCase() + " notification.";
        };
    }
    
    /**
     * Enrich a template model with branding properties from configuration
     */
    private Map<String, Object> enrichTemplateModel(Map<String, Object> originalModel) {
        Map<String, Object> enrichedModel = new HashMap<>();
        
        // Add original model data
        if (originalModel != null) {
            enrichedModel.putAll(originalModel);
        }
        
        // Add branding properties
        enrichedModel.put("companyName", brandingProperties.getCompanyName());
        enrichedModel.put("supportEmail", brandingProperties.getSupportEmail());
        enrichedModel.put("loginUrl", brandingProperties.getLoginUrl());
        enrichedModel.put("logoUrl", brandingProperties.getLogoUrl());
        enrichedModel.put("baseUrl", brandingProperties.getBaseUrl());
        enrichedModel.put("bannerUrl", brandingProperties.getBannerUrl());
        enrichedModel.put("currentYear", String.valueOf(Year.now().getValue()));
        enrichedModel.put("platformFeatures", brandingProperties.getPlatformFeatures());

        return enrichedModel;
    }
}
