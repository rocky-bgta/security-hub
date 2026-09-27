package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.EmailController;
import com.aspire.asat.registration.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailControllerImpl implements EmailController {

    private final EmailService emailService;

    public EmailControllerImpl(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public ResponseEntity<String> sendInvitationEmail(String token) {
        emailService.sendInvitationEmail(token);
        return ResponseEntity.ok("Email sent successfully");
    }
}
