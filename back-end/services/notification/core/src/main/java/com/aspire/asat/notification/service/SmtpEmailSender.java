package com.aspire.asat.notification.service;

import com.aspire.asat.notification.dto.EmailDto;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface SmtpEmailSender {
    void sendEmail(String to, String subject, String htmlBody) throws IOException, MessagingException;
    void sendEmailWithTemplate(String to, String subject, String templateId, java.util.Map<String, Object> templateModel) throws IOException, MessagingException;
    void sendEmailWithAttachments(EmailDto emailDto) throws IOException, MessagingException;
}
