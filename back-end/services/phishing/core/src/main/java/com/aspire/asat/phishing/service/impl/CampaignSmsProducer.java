package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.SendingPattern;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.aspire.asat.phishing.model.*;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.CampaignSmsPublishService;
import com.aspire.asat.phishing.service.SmsServerConfigurationService;
import com.aspire.asat.phishing.service.UrlShortenerService;
import com.aspire.asat.phishing.service.support.SmsTemplateValidator;
import com.aspire.asat.phishing.service.support.TemplatePersonalizationService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
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
public class CampaignSmsProducer implements CampaignSmsPublishService {

    private static final int MAX_SQS_DELAY_SECONDS = 900;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final CampaignRepository campaignRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final LandingPageRepository landingPageRepository;
    private final TrackingBaseUrlResolver trackingBaseUrlResolver;
    private final SmsServerConfigurationService smsServerConfigurationService;
    private final SmsTemplateValidator smsTemplateValidator;
    private final TemplatePersonalizationService personalizationService;
    private final UrlShortenerService urlShortenerService;

    @Value("${aws.sqs.campaign-sms-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.campaign-sms-publish.max-attempts:2}")
    private int publishMaxAttempts;

    @Value("${aws.sqs.campaign-sms-publish.initial-backoff-ms:250}")
    private long publishInitialBackoffMs;

    @Async
    @Override
    public void publishCampaignSms(String campaignId) {
        log.info("Publishing campaign SMS to SQS for campaign: {}", campaignId);
        try {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignId));

            EmailTemplate template = emailTemplateRepository.findById(campaign.getEmailTemplateId())
                    .orElseThrow(() -> new RuntimeException("SMS template not found: " + campaign.getEmailTemplateId()));

            if (template.getTemplateType() != TemplateType.SMS) {
                throw new IllegalStateException("Template is not an SMS template: " + template.getId());
            }

            SmsServerConfiguration smsServer = smsServerConfigurationService.resolveForCampaign(
                    campaign.getClientId(), campaign.getSmsServerConfigurationId());

            List<CampaignRecipient> pendingRecipients =
                    recipientRepository.findByCampaignIdAndStatus(campaignId, RecipientStatus.PENDING);

            if (pendingRecipients.isEmpty()) {
                log.warn("No pending recipients for SMS campaign: {}", campaignId);
                return;
            }

            LandingPage landingPage = null;
            if (campaign.getLandingPageId() != null) {
                landingPage = landingPageRepository.findById(campaign.getLandingPageId()).orElse(null);
            }
            String baseUrl = trackingBaseUrlResolver.resolve(
                    campaign.getClientId(),
                    campaign.getTrackingDomainId(),
                    landingPage != null ? landingPage.getTrackingDomainId() : null);
            String landingDomainId = landingPage != null ? landingPage.getTrackingDomainId() : null;
            String shortOrigin = trackingBaseUrlResolver.resolveShortLinkOrigin(
                    campaign.getClientId(),
                    campaign.getTrackingDomainId(),
                    landingDomainId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Verified tracking domain is required to shorten SMS URLs for campaign "
                                    + campaignId));

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

            List<String> failedRecipientIds = new ArrayList<>();
            for (int i = 0; i < pendingRecipients.size(); i++) {
                CampaignRecipient recipient = pendingRecipients.get(i);
                int batchIndex = i / batchSize;
                int delaySec = batchIndex * delayPerBatch;

                String trackingUrl = baseUrl + "/t/phish/" + recipient.getTrackingId();
                String smsUrl = urlShortenerService.shorten(trackingUrl, shortOrigin, recipient);
                smsTemplateValidator.validateRenderedLength(template.getSmsBody(), smsUrl, recipient);
                String messageBody = smsTemplateValidator.renderSmsBody(template.getSmsBody(), smsUrl, recipient);
                String phone = PhoneNumberUtils.normalize(recipient.getPhoneNumber());

                CampaignSmsMessage message = CampaignSmsMessage.builder()
                        .channel(CampaignChannel.SMS)
                        .campaignId(campaignId)
                        .recipientId(recipient.getId())
                        .clientId(campaign.getClientId())
                        .trackingId(recipient.getTrackingId())
                        .toPhone(phone)
                        .messageBody(messageBody)
                        .smsServerConfigurationId(smsServer.getId())
                        .campaignName(campaign.getCampaignName())
                        .build();

                if (!publishToSqs(message, delaySec)) {
                    failedRecipientIds.add(recipient.getId());
                }
            }

            if (!failedRecipientIds.isEmpty()) {
                throw new IllegalStateException("Failed to enqueue " + failedRecipientIds.size()
                        + " SMS message(s) for campaign " + campaignId);
            }
            log.info("Published {} SMS messages to SQS for campaign {}", pendingRecipients.size(), campaignId);
        } catch (Exception e) {
            log.error("Failed to publish campaign SMS for campaign: {}", campaignId, e);
        }
    }

    private boolean publishToSqs(CampaignSmsMessage message, int delaySeconds) {
        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize SMS SQS message for recipient: {}", message.getRecipientId(), e);
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
                    log.error("Failed to publish SMS SQS message for recipient {}", message.getRecipientId(), e);
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
