package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.sqs.MicroContentMessage;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.MicroContentSqsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicroContentSqsServiceImpl implements MicroContentSqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.micro-content-queue-url}")
    private String queueUrl;

    @Override
    public void sendMessage(MicroContentMessage message) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build());
        } catch (Exception e) {
            log.error("Failed to publish micro content message for jobId={}", message.getJobId(), e);
            throw new ServiceException("Failed to queue micro content job", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
