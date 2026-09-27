package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.sqs.DeepfakeRenderMessage;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.DeepfakeSqsService;
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
public class DeepfakeSqsServiceImpl implements DeepfakeSqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.deepfake-render-queue-url}")
    private String queueUrl;

    @Override
    public void sendMessage(DeepfakeRenderMessage message) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build());
        } catch (Exception e) {
            log.error("Failed to publish deepfake render message for renderId={}", message.getRenderId(), e);
            throw new ServiceException("Failed to queue deepfake render job", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
