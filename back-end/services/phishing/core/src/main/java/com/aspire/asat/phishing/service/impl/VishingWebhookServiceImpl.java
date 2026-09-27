package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.dto.request.VoiceCallStatusRequest;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.VishingWebhookService;
import com.aspire.asat.phishing.service.VoiceIngestionService;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.voice.TwilioSignatureValidator;
import com.aspire.asat.phishing.voice.VishingOutcomeResolution;
import com.aspire.asat.phishing.voice.VishingOutcomeResolver;
import com.aspire.asat.phishing.voice.VishingTwimlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VishingWebhookServiceImpl implements VishingWebhookService {

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final VishingCallLogRepository callLogRepository;
    private final VoiceServerConfigurationService voiceServerConfigurationService;
    private final CredentialEncryptionService credentialEncryptionService;
    private final DeepfakeS3Service deepfakeS3Service;
    private final VishingTwimlBuilder twimlBuilder;
    private final VishingOutcomeResolver outcomeResolver;
    private final TwilioSignatureValidator signatureValidator;
    private final VoiceIngestionService voiceIngestionService;

    @Value("${tracking.base-url}")
    private String trackingBaseUrl;

    @Value("${voice.twilio.validate-signature:true}")
    private boolean validateSignature;

    @Value("${voice.twilio.messages.compromised:Thank you. Your request has been processed. Goodbye.}")
    private String compromisedMessage;

    @Value("${voice.twilio.messages.engaged:Thank you for your response. Goodbye.}")
    private String engagedMessage;

    @Value("${voice.twilio.messages.answered:Thank you. Goodbye.}")
    private String answeredMessage;

    @Override
    public String buildCallTwiml(String trackingId, Map<String, String> params, String signature) {
        CampaignRecipient recipient = findRecipient(trackingId);
        Campaign campaign = findCampaign(recipient);
        validateSignature(campaign, trackingId, WebApiUrlConstants.VOICE_CALL_TWIML, params, signature);

        VishingInteractionMode mode = resolveMode(campaign);
        VishingCallLog callLog = callLogRepository.findByTrackingId(trackingId).orElse(null);

        String audioUrl = null;
        if (callLog != null && StringUtils.hasText(callLog.getAudioS3Key())) {
            audioUrl = deepfakeS3Service.presignGetUrl(callLog.getAudioS3Key());
        }
        String fallbackText = resolveFallbackText(callLog, campaign);
        String actionUrl = buildCallbackUrl(trackingId, WebApiUrlConstants.VOICE_CALL_GATHER);

        return twimlBuilder.buildGatherTwiml(audioUrl, fallbackText, actionUrl, mode);
    }

    @Override
    public String handleGather(String trackingId, Map<String, String> params, String signature) {
        CampaignRecipient recipient = findRecipient(trackingId);
        Campaign campaign = findCampaign(recipient);
        validateSignature(campaign, trackingId, WebApiUrlConstants.VOICE_CALL_GATHER, params, signature);

        VishingInteractionMode mode = resolveMode(campaign);
        String digits = params.get("Digits");
        String speechResult = params.get("SpeechResult");

        VishingOutcomeResolution resolution = outcomeResolver.resolve(
                digits, speechResult, mode, campaign.getSuccessKeywords());

        VoiceCallResultRequest request = VoiceCallResultRequest.builder()
                .outcome(resolution.outcome())
                .sensitiveDataCaptured(resolution.sensitiveDataCaptured())
                .detectedKeywords(resolution.detectedKeywords())
                .transcript(buildTranscript(digits, speechResult))
                .build();
        voiceIngestionService.processCallResult(trackingId, request);

        return twimlBuilder.buildClosingTwiml(resolveClosingMessage(resolution.outcome()));
    }

    @Override
    public void handleStatusCallback(String trackingId, Map<String, String> params, String signature) {
        CampaignRecipient recipient = findRecipient(trackingId);
        Campaign campaign = findCampaign(recipient);
        validateSignature(campaign, trackingId, WebApiUrlConstants.VOICE_CALL_TWILIO_STATUS, params, signature);

        String callStatus = params.get("CallStatus");
        applyCallProgressToLog(trackingId, callStatus, params);

        RecipientStatus mapped = mapTwilioStatus(callStatus);
        if (mapped == null) {
            return;
        }
        voiceIngestionService.recordCallStatus(trackingId,
                VoiceCallStatusRequest.builder().status(mapped).build());
    }

    /**
     * Persists call timing onto the {@link VishingCallLog} from Twilio's progress
     * callbacks: answer time on {@code in-progress}, and the authoritative
     * {@code CallDuration} plus end time on {@code completed}. The {@code completed}
     * callback is Twilio's final event, so it overwrites the placeholder duration
     * written when the gather outcome was recorded.
     */
    private void applyCallProgressToLog(String trackingId, String callStatus, Map<String, String> params) {
        if (!StringUtils.hasText(callStatus)) {
            return;
        }
        String status = callStatus.trim().toLowerCase(Locale.ROOT);
        if (!"in-progress".equals(status) && !"completed".equals(status)) {
            return;
        }
        callLogRepository.findByTrackingId(trackingId).ifPresent(logEntry -> {
            Instant now = Instant.now();
            if ("in-progress".equals(status)) {
                if (logEntry.getStartedAt() == null) {
                    logEntry.setStartedAt(now);
                }
            } else {
                logEntry.setEndedAt(now);
                Integer callDuration = parsePositiveInt(params.get("CallDuration"));
                if (callDuration != null) {
                    logEntry.setDurationSeconds(callDuration);
                    if (logEntry.getStartedAt() == null) {
                        logEntry.setStartedAt(now.minusSeconds(callDuration));
                    }
                } else if (logEntry.getStartedAt() != null) {
                    logEntry.setDurationSeconds(
                            (int) Math.max(0, Duration.between(logEntry.getStartedAt(), now).getSeconds()));
                }
            }
            callLogRepository.save(logEntry);
        });
    }

    private Integer parsePositiveInt(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Human-readable capture of the target's response for the dashboard transcript:
     * spoken words when speech was recognized, otherwise the DTMF digits entered.
     */
    private String buildTranscript(String digits, String speechResult) {
        if (StringUtils.hasText(speechResult)) {
            return speechResult.trim();
        }
        if (StringUtils.hasText(digits)) {
            return "DTMF digits entered: " + digits.trim();
        }
        return null;
    }

    private CampaignRecipient findRecipient(String trackingId) {
        return recipientRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found for tracking id"));
    }

    private Campaign findCampaign(CampaignRecipient recipient) {
        return campaignRepository.findById(recipient.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
    }

    private VishingInteractionMode resolveMode(Campaign campaign) {
        if (campaign.getVoiceScenario() != null && campaign.getVoiceScenario().getInteractionMode() != null) {
            return campaign.getVoiceScenario().getInteractionMode();
        }
        return VishingInteractionMode.BOTH;
    }

    private String resolveFallbackText(VishingCallLog callLog, Campaign campaign) {
        if (callLog != null && StringUtils.hasText(callLog.getRenderedScript())) {
            return callLog.getRenderedScript();
        }
        if (campaign.getVoiceScenario() != null) {
            return campaign.getVoiceScenario().getScriptBody();
        }
        return null;
    }

    private void validateSignature(Campaign campaign, String trackingId, String pathTemplate,
                                  Map<String, String> params, String signature) {
        if (!validateSignature) {
            return;
        }
        VoiceServerConfiguration config = resolveConfig(campaign);
        if (config.getProvider() != VoiceProviderType.TWILIO) {
            // Signature validation is Twilio-specific; other providers use their own auth.
            return;
        }
        String authToken = credentialEncryptionService.decrypt(config.getApiSecret());
        String url = buildCallbackUrl(trackingId, pathTemplate);
        if (!signatureValidator.isValid(authToken, url, params, signature)) {
            throw new ServiceException("Invalid Twilio signature", HttpStatus.FORBIDDEN);
        }
    }

    private VoiceServerConfiguration resolveConfig(Campaign campaign) {
        String configId = campaign.getTelephonyData() != null
                ? campaign.getTelephonyData().getVoiceServerConfigurationId()
                : campaign.getVoiceServerConfigurationId();
        return voiceServerConfigurationService.resolveForCampaign(campaign.getClientId(), configId);
    }

    private RecipientStatus mapTwilioStatus(String callStatus) {
        if (!StringUtils.hasText(callStatus)) {
            return null;
        }
        return switch (callStatus.trim().toLowerCase(Locale.ROOT)) {
            case "ringing" -> RecipientStatus.CALL_RINGING;
            case "no-answer" -> RecipientStatus.NO_ANSWER;
            case "busy", "failed", "canceled" -> RecipientStatus.CALL_FAILED;
            default -> null;
        };
    }

    private String resolveClosingMessage(VishingCallOutcome outcome) {
        return switch (outcome) {
            case COMPROMISED -> compromisedMessage;
            case ENGAGED -> engagedMessage;
            default -> answeredMessage;
        };
    }

    private String buildCallbackUrl(String trackingId, String pathTemplate) {
        String base = trackingBaseUrl.endsWith("/")
                ? trackingBaseUrl.substring(0, trackingBaseUrl.length() - 1)
                : trackingBaseUrl;
        String path = pathTemplate.replace("{trackingId}", trackingId);
        return base + WebApiUrlConstants.VOICE_BASE_PATH + path;
    }
}
