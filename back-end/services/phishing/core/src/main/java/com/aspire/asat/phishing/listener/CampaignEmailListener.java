package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.dto.sqs.CampaignEmailMessage;
import com.aspire.asat.phishing.service.impl.CampaignEmailConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

/**
 * Polls the phishing campaign email SQS queue and delegates
 * each message to {@link CampaignEmailConsumer} for sending.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignEmailListener {

    private final SqsClient sqsClient;
    private final CampaignEmailConsumer campaignEmailConsumer;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.campaign-email-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.campaign-email-consumer.max-messages-per-receive:10}")
    private int maxMessagesPerReceive;

    @Value("${aws.sqs.campaign-email-consumer.wait-time-seconds:20}")
    private int waitTimeSeconds;

    @Value("${aws.sqs.campaign-email-consumer.max-receive-cycles-per-poll:20}")
    private int maxReceiveCyclesPerPoll;

    @Value("${aws.sqs.campaign-email-consumer.visibility-timeout-seconds:180}")
    private int visibilityTimeoutSeconds;

    @Scheduled(
            fixedDelayString = "${aws.sqs.campaign-email-consumer.poll-fixed-delay-ms:2000}",
            scheduler = "campaignEmailScheduler")
    public void pollMessages() {
        log.debug("Polling SQS queue {} for campaign email messages", queueUrl);
        try {
            int totalReceived = 0;
            int totalProcessed = 0;

            for (int cycle = 1; cycle <= maxReceiveCyclesPerPoll; cycle++) {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(maxMessagesPerReceive)
                        .waitTimeSeconds(waitTimeSeconds)
                        .visibilityTimeout(visibilityTimeoutSeconds)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                if (messages.isEmpty()) {
                    if (cycle == 1) {
                        log.trace("SQS poll returned 0 messages (normal: SQS may return subset per call or eventual consistency)");
                    }
                    break;
                }

                totalReceived += messages.size();
                log.info("{} campaign email messages received from SQS (cycle {}/{})",
                        messages.size(), cycle, maxReceiveCyclesPerPoll);
                messages.forEach(msg ->
                        log.debug("*Received SQS messageId={}, receiptHandle={}, bodySize={}",
                                msg.messageId(), msg.receiptHandle(), msg.body() != null ? msg.body().length() : 0));

                for (Message message : messages) {
                    processMessage(message);
                    totalProcessed++;
                }
            }

            if (totalReceived > 0) {
                log.info("SQS polling run completed: received={}, processed={}", totalReceived, totalProcessed);
            }
        } catch (Exception e) {
            log.error("Error polling campaign email SQS queue: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) {
        try {
            CampaignEmailMessage emailMessage =
                    objectMapper.readValue(message.body(), CampaignEmailMessage.class);

            campaignEmailConsumer.processEmailMessage(emailMessage);

            deleteMessage(message);
            log.debug("Campaign email message {} processed and deleted", message.messageId());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse campaign email message {}. Will be retried/DLQ'd.",
                    message.messageId(), e);
        } catch (Exception e) {
            log.error("Failed to process campaign email message {}: {}. Will be retried/DLQ'd.",
                    message.messageId(), e.getMessage(), e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }
}
