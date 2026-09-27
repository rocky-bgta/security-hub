package com.aspire.asat.notification.service.impl;

import com.aspire.asat.notification.service.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final SesClient sesClient;
    
    @Value("${aws.ses.sandbox-mode:true}")
    private boolean sandboxMode;
    
    @Value("${aws.ses.fallback-to-console:false}")
    private boolean fallbackToConsole;

    @Override
    public void send(MimeMessage mimeMessage) throws IOException, MessagingException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            mimeMessage.writeTo(outputStream);
            RawMessage rawMessage = RawMessage.builder()
                    .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray())))
                    .build();

            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build();

            sesClient.sendRawEmail(rawEmailRequest);
            log.info("***Email sent successfully via SES.");
        } catch (SesException e) {
            String errorMessage = e.awsErrorDetails().errorMessage();
            log.error("SES Error during email sending: {}", errorMessage);
            
            // Handle SES sandbox mode errors gracefully
            if (isSandboxError(errorMessage) && fallbackToConsole) {
                logEmailToConsole(mimeMessage);
                log.warn("Email logged to console due to SES sandbox restrictions. In production, ensure recipient emails are verified.");
                return; // Don't throw exception, just log and continue
            }
            
            throw e; // Re-throw for other errors
        }
    }
    
    private boolean isSandboxError(String errorMessage) {
        return errorMessage != null && (
            errorMessage.contains("Email address is not verified") ||
            errorMessage.contains("MessageRejected") ||
            errorMessage.contains("sandbox")
        );
    }
    
    private void logEmailToConsole(MimeMessage mimeMessage) {
        try {
            log.info("=== EMAIL FALLBACK (SES Sandbox Mode) ===");
            log.info("To: {}", java.util.Arrays.toString(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)));
            log.info("Subject: {}", mimeMessage.getSubject());
            log.info("From: {}", mimeMessage.getFrom()[0]);
            log.info("Content-Type: {}", mimeMessage.getContentType());
            log.info("=== END EMAIL FALLBACK ===");
        } catch (Exception e) {
            log.error("Error logging email to console: {}", e.getMessage());
        }
    }
}
