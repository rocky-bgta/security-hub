package com.aspire.asat.notification.controller;

import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.service.SmtpEmailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = WebApiUrlConstants.NOTIFICATION_API + "/test", produces = "application/json")
public class TestEmailController {

    private final SmtpEmailSender smtpEmailSender;

    public TestEmailController(SmtpEmailSender smtpEmailSender) {
        this.smtpEmailSender = smtpEmailSender;
    }

    @PostMapping("/smtp/send")
    public ResponseEntity<ApiResponseDto<String>> sendTestEmail(@RequestBody EmailDto emailDto) {
        try {
            if (emailDto.getTemplateId() != null && !emailDto.getTemplateId().isEmpty()) {
                // Send templated email
                smtpEmailSender.sendEmailWithTemplate(
                    emailDto.getTo(),
                    emailDto.getSubject(),
                    emailDto.getTemplateId(),
                    emailDto.getTemplateModel()
                );
            } else {
                // Send simple HTML email (you can add a simple HTML body to EmailDto if needed)
                String simpleHtmlBody = "<h1>Test Email</h1><p>This is a test email sent via SMTP.</p>";
                smtpEmailSender.sendEmail(emailDto.getTo(), emailDto.getSubject(), simpleHtmlBody);
            }

            ApiResponseDto<String> response = new ApiResponseDto<>(
                "Email sent successfully via SMTP", 
                200, 
                "Email delivered to: " + emailDto.getTo()
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<String> response = new ApiResponseDto<>(
                "Failed to send email via SMTP: " + e.getMessage(), 
                500, 
                null
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/smtp/simple")
    public ResponseEntity<ApiResponseDto<String>> sendSimpleTestEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam(required = false) String body) {
        try {
            String htmlBody = body != null ? body : "<h1>Simple Test Email</h1><p>This is a simple test email sent via SMTP.</p>";
            smtpEmailSender.sendEmail(to, subject, htmlBody);

            ApiResponseDto<String> response = new ApiResponseDto<>(
                "Simple email sent successfully via SMTP", 
                200, 
                "Email delivered to: " + to
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<String> response = new ApiResponseDto<>(
                "Failed to send simple email via SMTP: " + e.getMessage(), 
                500, 
                null
            );
            return ResponseEntity.status(500).body(response);
        }
    }
}
