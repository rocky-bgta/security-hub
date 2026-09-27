package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.sqs.AiContentGenerationMessage;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.AiContentGenerationSqsService;
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
public class AiContentGenerationSqsServiceImpl implements AiContentGenerationSqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.ai-content-generation-queue-url}")
    private String queueUrl;

    @Override
    public void sendMessage(AiContentGenerationMessage message) {
        try {
            log.info("Publishing AI content generation message to SQS for jobId={} jobType={}",
                    message.getJobId(), message.getJobType());
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build());
        } catch (Exception e) {
            log.error("Failed to publish AI content generation message for jobId={} jobType={}",
                    message.getJobId(), message.getJobType(), e);
            throw new ServiceException("Failed to queue AI content generation job",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
