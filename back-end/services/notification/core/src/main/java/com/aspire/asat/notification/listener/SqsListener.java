package com.aspire.asat.notification.listener;

import com.aspire.asat.notification.dto.EmailDto;
import com.aspire.asat.notification.service.ConsumerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(SqsClient.class)
public class SqsListener {
    private final SqsClient sqsClient;
    private final ConsumerService consumerService;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.listener.thread-pool-size:5}")
    private int threadPoolSize;

    @Value("${aws.sqs.listener.wait-time-seconds:2}")
    private int waitTimeSeconds;

    private ExecutorService messageProcessingExecutor;

    @PostConstruct
    public void init() {
        messageProcessingExecutor = Executors.newFixedThreadPool(threadPoolSize);
        log.info("SQS listener initialized with thread pool size: {}, wait time: {}s", threadPoolSize, waitTimeSeconds);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SQS message processing executor...");
        messageProcessingExecutor.shutdown();
        try {
            if (!messageProcessingExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in 30s, forcing shutdown");
                messageProcessingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Scheduled(fixedDelayString = "${aws.sqs.listener.poll-interval-ms:500}")
    public void pollMessages() {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(waitTimeSeconds)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            if (!messages.isEmpty()) {
                log.info("{} messages received from SQS, dispatching to thread pool.", messages.size());
                for (Message message : messages) {
                    messageProcessingExecutor.submit(() -> processMessage(message));
                }
            }
        } catch (Exception e) {
            log.error("Error during SQS polling: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) {
        try {
            EmailDto emailRequest = objectMapper.readValue(message.body(), EmailDto.class);
            log.info("Processing message: {}", emailRequest);
            consumerService.processEmailRequest(emailRequest);
            deleteMessage(message);
            log.info("Message {} processed and deleted successfully.", message.messageId());
        } catch (JsonProcessingException e) {
            log.error("Error parsing message {}. This is a poison pill and will be retried/DLQ'd.", message.messageId(), e);
        } catch (Exception e) {
            log.error("A non-recoverable error occurred processing message {}: {}. It will be retried/DLQ'd.", message.messageId(), e.getMessage());
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

}
