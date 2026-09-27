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

        // Create a transport
        Transport transport = session.getTransport();

        try {
            log.info("Connecting to SMTP server - Host: {}, Port: {}, Username: {}", smtpHost, smtpPort, smtpUsername);
            
            // Connect to Amazon SES using the SMTP username and password
            transport.connect(smtpHost, smtpUsername, smtpPassword);
            log.info("SMTP connection established successfully");
            
            // Send the email
            transport.sendMessage(msg, msg.getAllRecipients());
            log.info("Email sent successfully via SMTP to: {}", to);
            
        } catch (Exception ex) {
            log.error("Failed to send email via SMTP: {}", ex.getMessage(), ex);
            log.error("SMTP Configuration - Host: {}, Port: {}, Username: {}", smtpHost, smtpPort, smtpUsername);
            throw ex;
        } finally {
            // Close and terminate the connection
            transport.close();
        }
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

        // Create a transport
        Transport transport = session.getTransport();

        try {
            log.info("Connecting to SMTP server - Host: {}, Port: {}, Username: {}", smtpHost, smtpPort, smtpUsername);
            
            // Connect to Amazon SES using the SMTP username and password
            transport.connect(smtpHost, smtpUsername, smtpPassword);
            log.info("SMTP connection established successfully");
            
            // Send the email
            transport.sendMessage(msg, msg.getAllRecipients());
            log.info("Email with attachments sent successfully via SMTP to: {}", emailDto.getTo());
            
        } catch (Exception ex) {
            log.error("Failed to send email with attachments via SMTP: {}", ex.getMessage(), ex);
            log.error("SMTP Configuration - Host: {}, Port: {}, Username: {}", smtpHost, smtpPort, smtpUsername);
            throw ex;
        } finally {
            // Close and terminate the connection
            transport.close();
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
