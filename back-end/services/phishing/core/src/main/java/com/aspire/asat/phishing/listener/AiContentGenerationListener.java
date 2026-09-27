package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.service.AiContentGenerationWorker;
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

/**
 * Polls the {@code ai-content-generation-queue} and dispatches each message to a
 * dedicated worker pool so a long-running AI vendor call cannot stall polling.
 *
 * <p>Always deletes messages after the worker returns: the worker persists any failure to
 * MongoDB, so SQS-level redelivery is suppressed (otherwise the same prompt would be charged
 * to the AI vendor multiple times). True infrastructural failures (worker thread death) will
 * still trigger SQS redelivery via visibility-timeout expiry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiContentGenerationListener {

    private final SqsClient sqsClient;
    private final AiContentGenerationWorker aiContentGenerationWorker;

    @Qualifier("aiContentGenerationWorkerExecutor")
    private final Executor aiContentGenerationWorkerExecutor;

    @Value("${aws.sqs.ai-content-generation-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.ai-content-generation-consumer.max-messages-per-receive:5}")
    private int maxMessagesPerReceive;

    @Value("${aws.sqs.ai-content-generation-consumer.wait-time-seconds:20}")
    private int waitTimeSeconds;

    @Value("${aws.sqs.ai-content-generation-consumer.visibility-timeout-seconds:240}")
    private int visibilityTimeoutSeconds;

    @Scheduled(
            fixedDelayString = "${aws.sqs.ai-content-generation-consumer.poll-fixed-delay-ms:2000}",
            scheduler = "aiContentGenerationScheduler")
    public void pollMessages() {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessagesPerReceive)
                    .waitTimeSeconds(waitTimeSeconds)
                    .visibilityTimeout(visibilityTimeoutSeconds)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();
            if (messages.isEmpty()) {
                return;
            }

            log.info("{} AI content generation messages received from SQS", messages.size());
            for (Message message : messages) {
                log.info("Dispatching AI content generation messageId={} to worker", message.messageId());
                aiContentGenerationWorkerExecutor.execute(() -> handle(message));
            }
        } catch (Exception e) {
            log.error("Error polling AI content generation SQS queue {}: {}",
                    queueUrl, e.getMessage(), e);
        }
    }

    private void handle(Message message) {
        try {
            aiContentGenerationWorker.process(message);
        } catch (Exception e) {
            // The worker is expected to swallow all expected failures; reaching here means
            // an unexpected runtime error escaped. Do not delete: let SQS redeliver after
            // visibility-timeout so the failure is at least retried once.
            log.error("Unhandled error processing AI content generation messageId={}",
                    message.messageId(), e);
            return;
        }
        deleteMessage(message);
    }

    private void deleteMessage(Message message) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete AI content generation messageId={}",
                    message.messageId(), e);
        }
    }
}
