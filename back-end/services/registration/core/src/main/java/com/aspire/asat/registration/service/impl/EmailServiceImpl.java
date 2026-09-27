package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Override
    public void sendInvitationEmail(String token) {
        System.out.println("Message is send successfully from registration service to producer");
    }
}
