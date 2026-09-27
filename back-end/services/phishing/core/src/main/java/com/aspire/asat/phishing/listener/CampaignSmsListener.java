package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.aspire.asat.phishing.service.impl.CampaignSmsConsumer;
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
 * Polls the phishing campaign SMS SQS queue and delegates
 * each message to {@link CampaignSmsConsumer} for sending.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignSmsListener {

    private final SqsClient sqsClient;
    private final CampaignSmsConsumer campaignSmsConsumer;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.campaign-sms-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.campaign-sms-consumer.max-messages-per-receive:10}")
    private int maxMessagesPerReceive;

    @Value("${aws.sqs.campaign-sms-consumer.wait-time-seconds:20}")
    private int waitTimeSeconds;

    @Value("${aws.sqs.campaign-sms-consumer.max-receive-cycles-per-poll:20}")
    private int maxReceiveCyclesPerPoll;

    @Value("${aws.sqs.campaign-sms-consumer.visibility-timeout-seconds:180}")
    private int visibilityTimeoutSeconds;

    @Scheduled(
            fixedDelayString = "${aws.sqs.campaign-sms-consumer.poll-fixed-delay-ms:2000}",
            scheduler = "campaignSmsScheduler")
    public void pollMessages() {
        log.debug("Polling SQS queue {} for campaign SMS messages", queueUrl);
        try {
            for (int cycle = 1; cycle <= maxReceiveCyclesPerPoll; cycle++) {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(maxMessagesPerReceive)
                        .waitTimeSeconds(waitTimeSeconds)
                        .visibilityTimeout(visibilityTimeoutSeconds)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();
                if (messages.isEmpty()) {
                    break;
                }

                log.info("{} campaign SMS messages received from SQS (cycle {}/{})",
                        messages.size(), cycle, maxReceiveCyclesPerPoll);

                for (Message message : messages) {
                    processMessage(message);
                }
            }
        } catch (Exception e) {
            log.error("Error polling campaign SMS SQS queue: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) {
        try {
            CampaignSmsMessage smsMessage = objectMapper.readValue(message.body(), CampaignSmsMessage.class);
            campaignSmsConsumer.processSmsMessage(smsMessage);
            deleteMessage(message);
            log.debug("Campaign SMS message {} processed and deleted", message.messageId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse campaign SMS message {}. Will be retried/DLQ'd.",
                    message.messageId(), e);
        } catch (Exception e) {
            log.error("Failed to process campaign SMS message {}: {}. Will be retried/DLQ'd.",
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
