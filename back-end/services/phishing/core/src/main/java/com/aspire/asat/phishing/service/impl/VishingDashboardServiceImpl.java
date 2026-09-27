package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.request.SuccessKeywordsRequest;
import com.aspire.asat.phishing.dto.request.TeachableMomentRequest;
import com.aspire.asat.phishing.dto.request.VoiceTestCallRequest;
import com.aspire.asat.phishing.dto.response.*;
import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.service.VishingDashboardService;
import com.aspire.asat.phishing.service.support.TemplatePersonalizationService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VishingDashboardServiceImpl implements VishingDashboardService {

    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final VishingCallLogRepository callLogRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final CampaignVoiceConsumer campaignVoiceConsumer;
    private final TemplatePersonalizationService templatePersonalizationService;
    private final ObjectMapper objectMapper;

    @Override
    public VishingDataCaptureDto getDataCapture(String campaignId, int offset, int pageSize) {
        Campaign campaign = findVoiceCampaign(campaignId);
        Page<VishingCallLog> page = callLogRepository.findByCampaignId(campaignId,
                PageRequest.of(Math.max(0, offset), Math.max(1, pageSize)));

        List<CallLogDto> logs = page.getContent().stream().map(this::toCallLogDto).collect(Collectors.toList());
        long compromised = callLogRepository.countByCampaignIdAndOutcome(campaignId, VishingCallOutcome.COMPROMISED);

        return VishingDataCaptureDto.builder()
                .totalCalls(page.getTotalElements())
                .compromisedCount(compromised)
                .callLogs(logs)
                .total(page.getTotalElements())
                .offset(offset)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public List<String> getSuccessKeywords(String campaignId) {
        Campaign campaign = findVoiceCampaign(campaignId);
        return campaign.getSuccessKeywords() != null ? campaign.getSuccessKeywords() : List.of();
    }

    @Override
    public void updateSuccessKeywords(String campaignId, SuccessKeywordsRequest request) {
        Campaign campaign = findVoiceCampaignForEdit(campaignId);
        campaign.setSuccessKeywords(request.getKeywords() != null ? request.getKeywords() : new ArrayList<>());
        campaignRepository.save(campaign);
    }

    @Override
    public VishingRemediationDto getRemediation(String campaignId) {
        Campaign campaign = findVoiceCampaign(campaignId);
        CampaignStats stats = campaign.getStats() != null ? campaign.getStats() : new CampaignStats();
        int total = Math.max(stats.getCallsTotal(), stats.getTotalRecipients());
        double answerRate = total > 0 ? (double) stats.getCallsAnswered() / total * 100 : 0;
        double engagedRate = total > 0 ? (double) stats.getCallsEngaged() / total * 100 : 0;
        double compromiseRate = total > 0 ? (double) stats.getCallsCompromised() / total * 100 : 0;
        double failureRate = total > 0 ? (double) stats.getCallsFailed() / total * 100 : 0;

        List<CampaignRecipient> recipients = recipientRepository.findByCampaignId(campaignId);
        double avgRisk = recipients.stream().mapToDouble(CampaignRecipient::getRiskScore).average().orElse(0);
        List<String> failedIds = recipients.stream()
                .filter(r -> r.getStatus() == RecipientStatus.CALL_FAILED || r.getStatus() == RecipientStatus.NO_ANSWER)
                .map(CampaignRecipient::getId)
                .collect(Collectors.toList());

        return VishingRemediationDto.builder()
                .answerRate(answerRate)
                .engagedRate(engagedRate)
                .compromiseRate(compromiseRate)
                .failureRate(failureRate)
                .averageRiskScore(avgRisk)
                .failedRecipientIds(failedIds)
                .trainingAssignedCount(stats.getTrainingAssignedCount())
                .trainingCompletedCount(stats.getTrainingCompletedCount())
                .build();
    }

    @Override
    public void sendTeachableMoment(String campaignId, TeachableMomentRequest request) {
        findVoiceCampaign(campaignId);
        recipientRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
        log.info("Teachable moment queued for campaign {} recipient {}", campaignId, request.getRecipientId());
    }

    @Override
    public VishingReportDto getReport(String campaignId) {
        Campaign campaign = findVoiceCampaign(campaignId);
        CampaignStats stats = campaign.getStats() != null ? campaign.getStats() : new CampaignStats();
        List<VishingCallLog> logs = callLogRepository.findByCampaignId(campaignId, PageRequest.of(0, 1000)).getContent();

        return VishingReportDto.builder()
                .campaignId(campaignId)
                .campaignName(campaign.getCampaignName())
                .totalRecipients(stats.getTotalRecipients())
                .callsTotal(stats.getCallsTotal())
                .callsAnswered(stats.getCallsAnswered())
                .callsEngaged(stats.getCallsEngaged())
                .callsReported(stats.getCallsReported())
                .callsCompromised(stats.getCallsCompromised())
                .callsNoAnswer(stats.getCallsNoAnswer())
                .callsFailed(stats.getCallsFailed())
                .retriesTriggered(stats.getRetriesTriggered())
                .averageSttLatencyMs(averageLatency(logs, VishingCallLog::getSttLatencyMs))
                .averageLlmLatencyMs(averageLatency(logs, VishingCallLog::getLlmLatencyMs))
                .averageTtsLatencyMs(averageLatency(logs, VishingCallLog::getTtsLatencyMs))
                .consentConfirmed(campaign.getVoiceData() != null && campaign.getVoiceData().isConsentConfirmed())
                .learningMode(campaign.getLearningMode() != null ? campaign.getLearningMode().name() : null)
                .build();
    }

    @Override
    public byte[] exportReport(String campaignId, String format, boolean anonymize) {
        VishingReportDto report = getReport(campaignId);
        try {
            if ("csv".equalsIgnoreCase(format)) {
                StringBuilder csv = new StringBuilder(
                        "campaignId,campaignName,callsTotal,callsAnswered,callsEngaged,callsReported,callsCompromised\n");
                csv.append(report.getCampaignId()).append(',')
                        .append(report.getCampaignName()).append(',')
                        .append(report.getCallsTotal()).append(',')
                        .append(report.getCallsAnswered()).append(',')
                        .append(report.getCallsEngaged()).append(',')
                        .append(report.getCallsReported()).append(',')
                        .append(report.getCallsCompromised()).append('\n');
                return csv.toString().getBytes(StandardCharsets.UTF_8);
            }
            return objectMapper.writeValueAsBytes(report);
        } catch (Exception e) {
            throw new PhishingValidationException("Failed to export report: " + e.getMessage());
        }
    }

    @Override
    public VoiceLiveMetricsDto getLiveMetrics(String campaignId) {
        findVoiceCampaign(campaignId);
        List<CampaignRecipient> recipients = recipientRepository.findByCampaignId(campaignId);
        int active = (int) recipients.stream()
                .filter(r -> r.getStatus() == RecipientStatus.CALL_QUEUED || r.getStatus() == RecipientStatus.CALL_RINGING)
                .count();
        int answered = (int) recipients.stream()
                .filter(r -> r.getStatus() == RecipientStatus.ANSWERED
                        || r.getStatus() == RecipientStatus.VOICE_ENGAGED
                        || r.getStatus() == RecipientStatus.COMPROMISED)
                .count();
        int engaged = (int) recipients.stream()
                .filter(r -> r.getStatus() == RecipientStatus.VOICE_ENGAGED
                        || r.getStatus() == RecipientStatus.COMPROMISED)
                .count();
        int completed = (int) recipients.stream()
                .filter(CampaignRecipient::satisfiesVoiceSimulatedCompletionCriteria)
                .count();

        List<VishingCallLog> logs = callLogRepository.findByCampaignId(campaignId, PageRequest.of(0, 500)).getContent();
        return VoiceLiveMetricsDto.builder()
                .activeCalls(active)
                .answeredCalls(answered)
                .engagedCalls(engaged)
                .completedCalls(completed)
                .averageSttLatencyMs(averageLatency(logs, VishingCallLog::getSttLatencyMs))
                .averageLlmLatencyMs(averageLatency(logs, VishingCallLog::getLlmLatencyMs))
                .averageTtsLatencyMs(averageLatency(logs, VishingCallLog::getTtsLatencyMs))
                .build();
    }

    @Override
    public void sendTestCall(String campaignId, VoiceTestCallRequest request) {
        Campaign campaign = findVoiceCampaign(campaignId);
        String phone = PhoneNumberUtils.normalize(request.getPhoneNumber());
        if (!PhoneNumberUtils.isValidE164(phone)) {
            throw new PhishingValidationException("Invalid phone number; use E.164 format");
        }

        CampaignRecipient recipient;
        if (request.getRecipientId() != null) {
            recipient = recipientRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
        } else {
            recipient = CampaignRecipient.builder()
                    .firstName("Test")
                    .lastName("User")
                    .phoneNumber(phone)
                    .trackingId("test-" + campaignId)
                    .build();
        }

        String script = campaign.getVoiceScenario() != null ? campaign.getVoiceScenario().getScriptBody() : "Test call";
        String rendered = templatePersonalizationService.personalize(script, recipient);

        CampaignVoiceMessage message = CampaignVoiceMessage.builder()
                .channel(CampaignChannel.VOICE)
                .campaignId(campaignId)
                .recipientId(recipient.getId() != null ? recipient.getId() : "test")
                .clientId(campaign.getClientId())
                .trackingId(recipient.getTrackingId())
                .toPhone(phone)
                .renderedScript(rendered)
                .voiceServerConfigurationId(campaign.getTelephonyData() != null
                        ? campaign.getTelephonyData().getVoiceServerConfigurationId()
                        : campaign.getVoiceServerConfigurationId())
                .externalVoiceId(campaign.getVoiceData() != null ? campaign.getVoiceData().getExternalVoiceId() : null)
                .voiceCloneProvider(campaign.getVoiceData() != null ? campaign.getVoiceData().getCloningEngine() : null)
                .language(campaign.getVoiceScenario() != null ? campaign.getVoiceScenario().getLanguage() : null)
                .callerId(campaign.getVoiceData() != null ? campaign.getVoiceData().getCallerId() : null)
                .campaignName(campaign.getCampaignName())
                .build();
        campaignVoiceConsumer.processVoiceMessage(message);
    }

    private CallLogDto toCallLogDto(VishingCallLog log) {
        Map<String, Object> sensitive = log.getSensitiveDataCaptured();
        boolean hasSensitive = sensitive != null && !sensitive.isEmpty();
        return CallLogDto.builder()
                .id(log.getId())
                .recipientId(log.getRecipientId())
                .recipientName(log.getRecipientName())
                .phoneNumber(log.getPhoneNumber())
                .status(log.getStatus())
                .outcome(log.getOutcome())
                .durationSeconds(log.getDurationSeconds())
                .retries(log.getRetries())
                .recordingS3Key(log.getRecordingS3Key())
                .transcript(log.getTranscript())
                .detectedKeywords(log.getDetectedKeywords())
                .sensitiveDataCaptured(sensitive)
                .scenario(hasSensitive ? log.getRenderedScript() : null)
                .startedAt(log.getStartedAt())
                .endedAt(log.getEndedAt())
                .build();
    }

    private Double averageLatency(List<VishingCallLog> logs, java.util.function.Function<VishingCallLog, Double> extractor) {
        return logs.stream().map(extractor).filter(v -> v != null && v > 0).mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private Campaign findVoiceCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        if (campaign.getChannel() != CampaignChannel.VOICE) {
            throw new PhishingValidationException("Campaign is not a voice campaign");
        }
        return campaign;
    }

    private Campaign findVoiceCampaignForEdit(String campaignId) {
        Campaign campaign = findVoiceCampaign(campaignId);
        if (!campaign.isEditable()) {
            throw new PhishingValidationException("Campaign is not editable");
        }
        return campaign;
    }
}
