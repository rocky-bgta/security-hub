package com.aspire.asat.notification.service.impl;

import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.service.NotificationProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.Optional;

@Slf4j
@Service
public class NotificationProducerServiceImpl implements NotificationProducerService {

    @Autowired(required = false)
    private Optional<SqsClient> sqsClient = Optional.empty();

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-name}")
    private String queueName;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Override
    public EmailDto queueEmailNotification(EmailDto emailDto) {
        if (sqsClient.isEmpty()) {
            log.warn("SQS client not available (AWS credentials not configured). Email not queued: {}", emailDto);
            throw new AspireException("AWS credentials not configured. Cannot queue email notification. Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY or configure aws.credentials in application config.");
        }
        try {
            String messageBody = objectMapper.writeValueAsString(emailDto);
            sqsClient.get().sendMessage(builder -> builder.queueUrl(queueUrl).messageBody(messageBody));
            log.info("Email notification queued successfully: {}", emailDto);
            return emailDto;
        } catch (Exception e) {
            log.error("Failed to queue email notification: {}", e.getMessage(), e);
            throw new AspireException("Failed to queue email notification", e);
        }
    }
}
