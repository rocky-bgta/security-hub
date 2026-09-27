package com.aspire.asat.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;

public interface EmailSender {
    void send(MimeMessage mimeMessage) throws IOException, MessagingException;
}
