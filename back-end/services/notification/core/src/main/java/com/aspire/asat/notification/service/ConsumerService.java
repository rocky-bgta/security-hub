package com.aspire.asat.notification.service;

import com.aspire.asat.notification.dto.EmailDto;

public interface ConsumerService {
    void processEmailRequest(EmailDto emailRequest);
}
