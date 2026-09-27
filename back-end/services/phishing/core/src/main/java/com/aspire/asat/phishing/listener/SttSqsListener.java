package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.service.SttTranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
public class SttSqsListener {

    private final SqsClient sqsClient;
    private final SttTranscriptionService sttTranscriptionService;
    @Qualifier("sttWorkerExecutor")
    private final Executor sttWorkerExecutor;

    @Value("${aws.sqs.stt-queue-url}")
    private String queueUrl;

    @Scheduled(fixedDelayString = "${stt.poll-interval-ms:2000}")
    public void pollMessages() {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(10)
                    .build();
            List<Message> messages = sqsClient.receiveMessage(request).messages();
            for (Message message : messages) {
                sttWorkerExecutor.execute(() -> process(message));
            }
        } catch (Exception e) {
            log.error("Failed polling STT queue {}", queueUrl, e);
        }
    }

    private void process(Message message) {
        try {
            sttTranscriptionService.processAudio(message);
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception e) {
            // Intentionally no delete. SQS retry + DLQ policy will handle failures.
            log.error("Failed processing STT SQS messageId={}", message.messageId(), e);
        }
    }
}
