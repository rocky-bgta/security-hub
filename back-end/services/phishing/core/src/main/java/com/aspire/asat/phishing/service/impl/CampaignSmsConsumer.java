package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.SmsDeliveryStatus;
import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.aspire.asat.phishing.exception.TransientSmsFailureException;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignSmsDelivery;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.CampaignSmsDeliveryRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.sms.SmsProvider;
import com.aspire.asat.phishing.sms.SmsProviderFactory;
import com.aspire.asat.phishing.sms.SmsSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignSmsConsumer {

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final CampaignSmsDeliveryRepository campaignSmsDeliveryRepository;
    private final SmsServerConfigurationRepository smsServerConfigurationRepository;
    private final SmsProviderFactory smsProviderFactory;
    private final TrackingServiceImpl trackingService;
    private final CampaignCompletionEvaluator completionEvaluator;

    public void processSmsMessage(CampaignSmsMessage message) {
        log.info("Sending SMS to {} for campaign {}", message.getToPhone(), message.getCampaignId());

        // Do not send (nor record activity) for campaigns that are no longer active (expired/cancelled/completed).
        if (isCampaignInactiveForSending(message.getCampaignId())) {
            log.info("Skipping send for inactive/expired campaign {} (recipientId={}, phone={})",
                    message.getCampaignId(), message.getRecipientId(), message.getToPhone());
            return;
        }

        SmsServerConfiguration config = smsServerConfigurationRepository.findById(message.getSmsServerConfigurationId())
                .orElseThrow(() -> new IllegalStateException("SMS server configuration not found"));

        SmsProvider provider = smsProviderFactory.create(config);
        SmsSendResult result;
        try {
            result = provider.send(message.getToPhone(), message.getMessageBody());
        } catch (Exception e) {
            if (isTransientFailure(e)) {
                throw new TransientSmsFailureException("Transient SMS failure", e);
            }
            result = SmsSendResult.fail("SMS_ERROR", e.getMessage());
        }

        if (result.success()) {
            onSendSuccess(message, config, result.messageId());
        } else {
            onSendFailure(message, config, result.errorMessage());
        }
    }

    private void onSendSuccess(CampaignSmsMessage message, SmsServerConfiguration config, String messageId) {
        Instant now = Instant.now();
        recipientRepository.markSentByIdOrTrackingId(message.getRecipientId(), message.getTrackingId(), now);
        recipientRepository.findById(message.getRecipientId()).ifPresent(recipient -> {
            recipient.setSmsSentAt(now);
            recipientRepository.save(recipient);
        });

        saveOrUpdateDelivery(message, config, messageId, SmsDeliveryStatus.SENT, now);
        saveActivity(message, ActivityType.SMS_SENT);
        incrementStat(message.getCampaignId(), true);

        recipientRepository.findById(message.getRecipientId())
                .ifPresent(recipient ->
                        trackingService.updateUserRiskProfilePhishingScore(recipient, ActivityType.SMS_SENT));
    }

    private void onSendFailure(CampaignSmsMessage message, SmsServerConfiguration config, String error) {
        Instant now = Instant.now();
        recipientRepository.markBouncedByIdOrTrackingId(message.getRecipientId(), message.getTrackingId(), now);
        saveOrUpdateDelivery(message, config, null, SmsDeliveryStatus.FAILED, now);
        saveActivity(message, ActivityType.SMS_FAILED);
        incrementStat(message.getCampaignId(), false);
        log.error("SMS send failed for {}: {}", message.getToPhone(), error);
    }

    private void saveOrUpdateDelivery(CampaignSmsMessage message, SmsServerConfiguration config,
                                      String messageId, SmsDeliveryStatus status, Instant sentAt) {
        Optional<CampaignSmsDelivery> existing =
                campaignSmsDeliveryRepository.findByCampaignIdAndRecipientId(
                        message.getCampaignId(), message.getRecipientId());
        CampaignSmsDelivery delivery = existing.orElseGet(() -> CampaignSmsDelivery.builder()
                .campaignId(message.getCampaignId())
                .recipientId(message.getRecipientId())
                .recipient(message.getToPhone())
                .provider(config.getProvider())
                .build());
        delivery.setMessageId(messageId);
        delivery.setStatus(status);
        delivery.setSentAt(sentAt);
        campaignSmsDeliveryRepository.save(delivery);
    }

    private void saveActivity(CampaignSmsMessage message, ActivityType activityType) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            if (message.getToPhone() != null) {
                metadata.put("phoneNumber", message.getToPhone());
            }
            EmailActivity activity = EmailActivity.builder()
                    .clientId(message.getClientId())
                    .campaignId(message.getCampaignId())
                    .recipientId(message.getRecipientId())
                    .recipientEmail(resolveActivityEmail(message.getRecipientId(), message.getToPhone()))
                    .campaignName(message.getCampaignName())
                    .trackingId(message.getTrackingId())
                    .activityType(activityType)
                    .channel(message.getChannel() != null ? message.getChannel() : CampaignChannel.SMS)
                    .timestamp(Instant.now())
                    .metadata(metadata)
                    .build();
            emailActivityRepository.save(activity);
        } catch (Exception e) {
            log.error("Failed to save SMS activity: {}", e.getMessage());
        }
    }

    /**
     * Report/dashboard aggregation groups by {@code recipientEmail}. Store the user's email
     * so SMS_SENT joins click events; keep the phone only as a fallback.
     */
    private String resolveActivityEmail(String recipientId, String fallbackPhone) {
        if (recipientId == null || recipientId.isBlank()) {
            return fallbackPhone;
        }
        return recipientRepository.findById(recipientId)
                .map(CampaignRecipient::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .orElse(fallbackPhone);
    }

    private void incrementStat(String campaignId, boolean success) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            if (success) {
                campaign.getStats().setSmsSent(campaign.getStats().getSmsSent() + 1);
            } else {
                campaign.getStats().setSmsFailed(campaign.getStats().getSmsFailed() + 1);
            }
            campaign.getStats().setLastUpdatedAt(Instant.now());
            completionEvaluator.evaluateAndApply(campaign);
            campaignRepository.save(campaign);
        });
    }

    /**
     * Returns true when the campaign no longer permits sending: expired, cancelled, or completed.
     * Mirrors the email consumer so in-flight SQS messages are not delivered after the campaign
     * is no longer active.
     */
    private boolean isCampaignInactiveForSending(String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return false;
        }
        return campaignRepository.findById(campaignId)
                .map(campaign -> campaign.isExpired()
                        || campaign.getStatus() == CampaignStatus.EXPIRED
                        || campaign.getStatus() == CampaignStatus.CANCELLED
                        || campaign.getStatus() == CampaignStatus.COMPLETED)
                .orElse(false);
    }

    private boolean isTransientFailure(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("timeout") || msg.contains("throttl") || msg.contains("rate")
                || msg.contains("temporary") || msg.contains("unavailable");
    }
}
