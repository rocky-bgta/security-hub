package com.aspire.asat.notification.service.impl;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.enums.ChannelStatus;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.service.ConsumerService;
import com.aspire.asat.notification.service.NotificationHistoryService;
import com.aspire.asat.notification.service.NotificationTemplateService;
import com.aspire.asat.notification.service.SmtpEmailSender;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final NotificationTemplateService templateService;
    private final SmtpEmailSender smtpEmailSender;
    private final NotificationHistoryService notificationHistoryService;


    @Override
    public void processEmailRequest(EmailDto emailRequest) {
        log.info("Processing email request for template '{}' to '{}'", emailRequest.getTemplateId(), emailRequest.getTo());

        String logId = extractLogId(emailRequest);

        try {
            NotificationTemplate template = fetchTemplate(emailRequest, logId);
            String htmlBody = generateHtmlBody(template, emailRequest, logId);

            sendEmail(emailRequest, htmlBody);

            updateNotificationStatus(logId, NotificationChannel.EMAIL, ChannelStatus.SUCCESS, null);
        } catch (Exception e) {
            handleProcessingError(emailRequest, logId, e);
        }
    }

    private String extractLogId(EmailDto emailRequest) {
        if (emailRequest.getMetadata() != null && emailRequest.getMetadata().containsKey("notificationLogId")) {
            return (String) emailRequest.getMetadata().get("notificationLogId");
        }
        return null;
    }

    private NotificationTemplate fetchTemplate(EmailDto emailRequest, String logId) {
        Optional<NotificationTemplate> templateOpt = templateService.getTemplateById(emailRequest.getTemplateId());
        if (templateOpt.isEmpty()) {
            String errorMessage = "Template not found with ID: " + emailRequest.getTemplateId();
            log.error(errorMessage);
            updateNotificationStatus(logId, NotificationChannel.EMAIL, ChannelStatus.FAILED, errorMessage);
            throw new AspireException(errorMessage);
        }
        NotificationTemplate template = templateOpt.get();
        return template;
    }

    private String generateHtmlBody(NotificationTemplate template, EmailDto emailRequest, String logId) {
        String htmlBody = templateService.generateHtmlContent(template, emailRequest.getTemplateModel());
        if (htmlBody == null || htmlBody.trim().isEmpty()) {
            String errorMessage = "Generated HTML body is empty for template: " + emailRequest.getTemplateId();
            log.error(errorMessage);
            updateNotificationStatus(logId, NotificationChannel.EMAIL, ChannelStatus.FAILED, errorMessage);
            throw new AspireException(errorMessage);
        }
        return htmlBody;
    }

    private void sendEmail(EmailDto emailRequest, String htmlBody) throws MessagingException, IOException {
        if (emailRequest.getAttachments() != null && !emailRequest.getAttachments().isEmpty()) {
            log.info("Sending email with {} attachments via SMTP", emailRequest.getAttachments().size());
            smtpEmailSender.sendEmailWithAttachments(emailRequest);
        } else {
            log.info("Sending templated email via SMTP");
            smtpEmailSender.sendEmailWithTemplate(
                    emailRequest.getTo(),
                    emailRequest.getSubject(),
                    emailRequest.getTemplateId(),
                    emailRequest.getTemplateModel()
            );
        }
    }

    private void updateNotificationStatus(String logId, NotificationChannel channel, ChannelStatus status, String message) {
        if (logId != null) {
            notificationHistoryService.updateChannelStatus(logId, channel, status, message);
        }
    }

    private void handleProcessingError(EmailDto emailRequest, String logId, Exception e) {
        log.error("Failed to process email for {}: {}", emailRequest.getTo(), e.getMessage(), e);
        updateNotificationStatus(logId, NotificationChannel.EMAIL, ChannelStatus.FAILED, e.getMessage());
        throw new AspireException("Email processing failed", e);
    }
}
