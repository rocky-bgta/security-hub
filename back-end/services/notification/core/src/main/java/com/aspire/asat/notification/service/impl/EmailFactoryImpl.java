package com.aspire.asat.notification.service.impl;

import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.service.EmailFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
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
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailFactoryImpl implements EmailFactory {

    private final S3Client s3Client;
    @Value("${aws.ses.sender-email}")
    private String senderEmail;


    @Override
    public MimeMessage createEmail(EmailDto request, String htmlBody) {

        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session);

            // Set headers
            mimeMessage.setSubject(request.getSubject(), "UTF-8");
            mimeMessage.setFrom(senderEmail);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, request.getTo());

            MimeMultipart multipart = new MimeMultipart("mixed");

            // Add HTML body part
            multipart.addBodyPart(createHtmlBodyPart(htmlBody));

            // Add attachments if they exist
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                for (AttachmentDto attachment : request.getAttachments()) {
                    multipart.addBodyPart(createAttachmentPart(attachment));
                }
            }
            mimeMessage.setContent(multipart);
            return mimeMessage;

        } catch (Exception e) {
            log.error("Error creating email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create email", e);
        }
    }

    private MimeBodyPart createHtmlBodyPart(String htmlBody) throws MessagingException {
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(htmlBody, "text/html; charset=UTF-8");
        return bodyPart;
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
