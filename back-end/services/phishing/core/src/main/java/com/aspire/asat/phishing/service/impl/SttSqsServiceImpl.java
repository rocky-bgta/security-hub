package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.sqs.SttTranscriptionMessage;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.SttSqsService;
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
public class SttSqsServiceImpl implements SttSqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.stt-queue-url}")
    private String queueUrl;

    @Override
    public void sendMessage(SttTranscriptionMessage message) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build());
        } catch (Exception e) {
            log.error("Failed to publish STT message for audioId={}", message.getAudioId(), e);
            throw new ServiceException("Failed to queue transcription job", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
