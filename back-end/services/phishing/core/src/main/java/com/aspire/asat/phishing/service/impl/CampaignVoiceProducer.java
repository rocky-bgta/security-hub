package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.CampaignTelephonyData;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.SendingPattern;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.model.*;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.CampaignVoicePublishService;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import com.aspire.asat.phishing.service.support.TemplatePersonalizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignVoiceProducer implements CampaignVoicePublishService {

    private static final int MAX_SQS_DELAY_SECONDS = 900;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final VoiceServerConfigurationService voiceServerConfigurationService;
    private final TemplatePersonalizationService templatePersonalizationService;

    @Value("${aws.sqs.campaign-voice-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.campaign-voice-publish.max-attempts:2}")
    private int publishMaxAttempts;

    @Value("${aws.sqs.campaign-voice-publish.initial-backoff-ms:250}")
    private long publishInitialBackoffMs;

    @Async
    @Override
    public void publishCampaignVoice(String campaignId) {
        log.info("Publishing campaign voice to SQS for campaign: {}", campaignId);
        try {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignId));

            if (campaign.getVoiceScenario() == null || campaign.getVoiceScenario().getScriptBody() == null) {
                throw new IllegalStateException("Voice scenario script not configured for campaign: " + campaignId);
            }

            VoiceServerConfiguration voiceServer = voiceServerConfigurationService.resolveForCampaign(
                    campaign.getClientId(),
                    campaign.getTelephonyData() != null
                            ? campaign.getTelephonyData().getVoiceServerConfigurationId()
                            : campaign.getVoiceServerConfigurationId());

            List<CampaignRecipient> pendingRecipients =
                    recipientRepository.findByCampaignIdAndStatus(campaignId, RecipientStatus.PENDING);

            if (pendingRecipients.isEmpty()) {
                log.warn("No pending recipients for voice campaign: {}", campaignId);
                return;
            }

            SendingConfig sendingConfig = campaign.getSchedule() != null
                    ? campaign.getSchedule().getSendingConfig() : null;
            boolean staggered = sendingConfig != null
                    && sendingConfig.getPattern() == SendingPattern.STAGGERED
                    && sendingConfig.getBatchSize() > 0;

            int batchSize = pendingRecipients.size();
            int delayPerBatch = 0;
            if (staggered && sendingConfig != null) {
                batchSize = sendingConfig.getBatchSize();
                delayPerBatch = Math.min(sendingConfig.getBatchIntervalMinutes() * 60, MAX_SQS_DELAY_SECONDS);
            }

            CampaignTelephonyData telephony = campaign.getTelephonyData();
            int maxRetries = telephony != null && telephony.getRetryPolicy() != null
                    ? telephony.getRetryPolicy().getMaxRetries() : 3;
            int retryInterval = telephony != null && telephony.getRetryPolicy() != null
                    ? telephony.getRetryPolicy().getIntervalMinutes() : 15;

            String scriptTemplate = campaign.getVoiceScenario().getScriptBody();
            String externalVoiceId = campaign.getVoiceData() != null ? campaign.getVoiceData().getExternalVoiceId() : null;
            VoiceCloneProvider voiceCloneProvider = campaign.getVoiceData() != null
                    ? campaign.getVoiceData().getCloningEngine() : null;
            String language = campaign.getVoiceScenario().getLanguage();
            String callerId = campaign.getVoiceData() != null && campaign.getVoiceData().getCallerId() != null
                    ? campaign.getVoiceData().getCallerId()
                    : voiceServer.getCallerId();

            List<String> failedRecipientIds = new ArrayList<>();
            for (int i = 0; i < pendingRecipients.size(); i++) {
                CampaignRecipient recipient = pendingRecipients.get(i);
                int batchIndex = i / batchSize;
                int delaySec = batchIndex * delayPerBatch;

                String renderedScript = templatePersonalizationService.personalize(scriptTemplate, recipient);
                String phone = PhoneNumberUtils.normalize(recipient.getPhoneNumber());

                CampaignVoiceMessage message = CampaignVoiceMessage.builder()
                        .channel(CampaignChannel.VOICE)
                        .campaignId(campaignId)
                        .recipientId(recipient.getId())
                        .clientId(campaign.getClientId())
                        .trackingId(recipient.getTrackingId())
                        .toPhone(phone)
                        .renderedScript(renderedScript)
                        .voiceServerConfigurationId(voiceServer.getId())
                        .externalVoiceId(externalVoiceId)
                        .voiceCloneProvider(voiceCloneProvider)
                        .language(language)
                        .callerId(callerId)
                        .campaignName(campaign.getCampaignName())
                        .maxRetries(maxRetries)
                        .retryIntervalMinutes(retryInterval)
                        .build();

                if (!publishToSqs(message, delaySec)) {
                    failedRecipientIds.add(recipient.getId());
                }
            }

            if (!failedRecipientIds.isEmpty()) {
                throw new IllegalStateException("Failed to enqueue " + failedRecipientIds.size()
                        + " voice message(s) for campaign " + campaignId);
            }
            log.info("Published {} voice messages to SQS for campaign {}", pendingRecipients.size(), campaignId);
        } catch (Exception e) {
            log.error("Failed to publish campaign voice for campaign: {}", campaignId, e);
        }
    }

    private boolean publishToSqs(CampaignVoiceMessage message, int delaySeconds) {
        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize voice SQS message for recipient: {}", message.getRecipientId(), e);
            return false;
        }

        for (int attempt = 1; attempt <= publishMaxAttempts; attempt++) {
            try {
                SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody);
                if (delaySeconds > 0) {
                    requestBuilder.delaySeconds(delaySeconds);
                }
                sqsClient.sendMessage(requestBuilder.build());
                return true;
            } catch (Exception e) {
                if (attempt >= publishMaxAttempts) {
                    log.error("Failed to publish voice SQS message for recipient {}", message.getRecipientId(), e);
                    return false;
                }
                long backoffMs = publishInitialBackoffMs * (1L << (attempt - 1));
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}
