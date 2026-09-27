package com.aspire.asat.notification.service;

import com.aspire.asat.notification.dto.EmailDto;

public interface NotificationProducerService {
    EmailDto queueEmailNotification(EmailDto emailDto);
}
