package com.aspire.asat.notification.service.impl;

import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.service.NotificationTemplateService;
import com.aspire.asat.notification.service.SmtpEmailSender;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailSenderImpl implements SmtpEmailSender {

    private final NotificationTemplateService templateService;
    private final S3Client s3Client;

    @Value("${aws.ses.smtp.host}")
    private String smtpHost;

    @Value("${aws.ses.smtp.port}")
    private int smtpPort;

    @Value("${aws.ses.smtp.username}")
    private String smtpUsername;

    @Value("${aws.ses.smtp.password}")
    private String smtpPassword;

    @Value("${aws.ses.sender-email}")
    private String fromEmail;

    @Value("${aws.ses.smtp.from-name}")
    private String fromName;

    @Value("${aws.ses.fallback-to-console:false}")
    private boolean fallbackToConsole;

    @Override
    public void sendEmail(String to, String subject, String htmlBody) throws IOException, MessagingException {
        log.info("Sending email via SMTP to: {}, subject: {}", to, subject);

        // Create a Properties object to contain connection configuration information
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail, fromName));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setContent(htmlBody, "text/html; charset=UTF-8");

        deliverMessage(msg, to, "email");
    }

    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateId, Map<String, Object> templateModel) throws IOException, MessagingException {
        log.info("Sending templated email via SMTP to: {}, template: {}", to, templateId);
        
        // Get template from database and generate HTML
        Optional<NotificationTemplate> templateOpt = templateService.getTemplateById(templateId);
        if (templateOpt.isEmpty()) {
            log.error("Template not found with ID: {}", templateId);
            throw new AspireException("Template not found with ID: " + templateId);
        }
        
        NotificationTemplate template = templateOpt.get();
        String htmlBody = templateService.generateHtmlContent(template, templateModel);
        
        if (htmlBody == null || htmlBody.trim().isEmpty()) {
            log.error("Generated HTML body is empty for template: {}", templateId);
            throw new AspireException("Generated HTML body is empty for template: " + templateId);
        }
        
        // Send the email
        sendEmail(to, subject, htmlBody);
    }

    @Override
    public void sendEmailWithAttachments(EmailDto emailDto) throws IOException, MessagingException {
        log.info("Sending email with attachments via SMTP to: {}, subject: {}", emailDto.getTo(), emailDto.getSubject());

        // Generate HTML from a template if templateId is provided
        String htmlBody;
        if (emailDto.getTemplateId() != null && !emailDto.getTemplateId().isEmpty()) {
            Optional<NotificationTemplate> templateOpt = templateService.getTemplateById(emailDto.getTemplateId());
            if (templateOpt.isEmpty()) {
                log.error("Template not found with ID: {}", emailDto.getTemplateId());
                throw new AspireException("Template not found with ID: " + emailDto.getTemplateId());
            }
            
            NotificationTemplate template = templateOpt.get();
            htmlBody = templateService.generateHtmlContent(template, emailDto.getTemplateModel());
            
            if (htmlBody == null || htmlBody.trim().isEmpty()) {
                log.error("Generated HTML body is empty for template: {}", emailDto.getTemplateId());
                throw new AspireException("Generated HTML body is empty for template: " + emailDto.getTemplateId());
            }
        } else {
            htmlBody = "<h1>Email</h1><p>This is an email sent via SMTP.</p>";
        }

        // Create a Properties object to contain connection configuration information
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail, fromName));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(emailDto.getTo()));
        msg.setSubject(emailDto.getSubject());

        // Create a multipart message for attachments
        MimeMultipart multipart = new MimeMultipart("mixed");

        // Add HTML body part
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(htmlBody, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlBodyPart);

        // Add attachments if they exist
        if (emailDto.getAttachments() != null && !emailDto.getAttachments().isEmpty()) {
            for (AttachmentDto attachment : emailDto.getAttachments()) {
                MimeBodyPart attachmentPart = createAttachmentPart(attachment);
                multipart.addBodyPart(attachmentPart);
            }
        }

        msg.setContent(multipart);

        deliverMessage(msg, emailDto.getTo(), "email with attachments");
    }

    private void deliverMessage(MimeMessage msg, String to, String description) throws MessagingException {
        if (fallbackToConsole && !hasSmtpCredentials()) {
            logEmailToConsole(msg, "SMTP credentials are not configured");
            return;
        }

        Transport transport = msg.getSession().getTransport();

        try {
            log.info("Connecting to SMTP server - Host: {}, Port: {}, Username: {}", smtpHost, smtpPort, smtpUsername);
            transport.connect(smtpHost, smtpUsername, smtpPassword);
            log.info("SMTP connection established successfully");

            transport.sendMessage(msg, msg.getAllRecipients());
            log.info("{} sent successfully via SMTP to: {}", description, to);

        } catch (Exception ex) {
            log.error("Failed to send {} via SMTP: {}", description, ex.getMessage(), ex);
            log.error("SMTP Configuration - Host: {}, Port: {}, Username: {}", smtpHost, smtpPort, smtpUsername);

            if (fallbackToConsole) {
                logEmailToConsole(msg, "SMTP delivery failed: " + ex.getMessage());
                return;
            }

            if (ex instanceof MessagingException messagingException) {
                throw messagingException;
            }
            throw new MessagingException("Failed to send " + description + " via SMTP", ex);
        } finally {
            transport.close();
        }
    }

    private boolean hasSmtpCredentials() {
        return StringUtils.hasText(smtpUsername) && StringUtils.hasText(smtpPassword);
    }

    private void logEmailToConsole(MimeMessage msg, String reason) {
        try {
            log.warn("=== SMTP EMAIL FALLBACK ({}) ===", reason);
            log.warn("To: {}", java.util.Arrays.toString(msg.getRecipients(Message.RecipientType.TO)));
            log.warn("From: {}", java.util.Arrays.toString(msg.getFrom()));
            log.warn("Subject: {}", msg.getSubject());
            log.warn("Content-Type: {}", msg.getContentType());
            Object content = msg.getContent();
            if (content instanceof String body) {
                log.warn("Body: {}", body);
            }
            log.warn("=== END SMTP EMAIL FALLBACK ===");
        } catch (Exception e) {
            log.error("Error logging SMTP fallback email to console: {}", e.getMessage(), e);
        }
    }

    private MimeBodyPart createAttachmentPart(AttachmentDto attachment) throws MessagingException, IOException {
        log.info("Downloading attachment from S3: bucket={}, key={}", attachment.getBucketName(), attachment.getObjectKey());

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(attachment.getBucketName())
                .key(attachment.getObjectKey())
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            byte[] fileBytes = s3Object.readAllBytes();

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setContent(fileBytes, s3Object.response().contentType());
            attachmentPart.setFileName(getFileNameFromKey(attachment.getObjectKey()));
            return attachmentPart;
        }
    }

    private String getFileNameFromKey(String key) {
        return key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
    }
}
