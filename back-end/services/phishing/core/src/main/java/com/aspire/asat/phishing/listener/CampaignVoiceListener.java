package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.service.impl.CampaignVoiceConsumer;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignVoiceListener {

    private final SqsClient sqsClient;
    private final CampaignVoiceConsumer campaignVoiceConsumer;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.campaign-voice-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.campaign-voice-consumer.max-messages-per-receive:10}")
    private int maxMessagesPerReceive;

    @Value("${aws.sqs.campaign-voice-consumer.wait-time-seconds:20}")
    private int waitTimeSeconds;

    @Value("${aws.sqs.campaign-voice-consumer.max-receive-cycles-per-poll:20}")
    private int maxReceiveCyclesPerPoll;

    @Value("${aws.sqs.campaign-voice-consumer.visibility-timeout-seconds:180}")
    private int visibilityTimeoutSeconds;

    @Scheduled(
            fixedDelayString = "${aws.sqs.campaign-voice-consumer.poll-fixed-delay-ms:2000}",
            scheduler = "campaignVoiceScheduler")
    public void pollMessages() {
        log.debug("Polling SQS queue {} for campaign voice messages", queueUrl);
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

                log.info("{} campaign voice messages received from SQS (cycle {}/{})",
                        messages.size(), cycle, maxReceiveCyclesPerPoll);

                for (Message message : messages) {
                    processMessage(message);
                }
            }
        } catch (Exception e) {
            log.error("Error polling campaign voice SQS queue: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) {
        try {
            CampaignVoiceMessage voiceMessage = objectMapper.readValue(message.body(), CampaignVoiceMessage.class);
            campaignVoiceConsumer.processVoiceMessage(voiceMessage);
            deleteMessage(message);
            log.debug("Campaign voice message {} processed and deleted", message.messageId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse campaign voice message {}. Will be retried/DLQ'd.",
                    message.messageId(), e);
        } catch (Exception e) {
            log.error("Failed to process campaign voice message {}: {}. Will be retried/DLQ'd.",
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
