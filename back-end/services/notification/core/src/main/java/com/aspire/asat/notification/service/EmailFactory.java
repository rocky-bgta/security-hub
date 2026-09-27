package com.aspire.asat.notification.service;

import com.aspire.asat.notification.dto.EmailDto;
import jakarta.mail.internet.MimeMessage;

public interface EmailFactory {
    MimeMessage createEmail(EmailDto request, String htmlBody);
}
