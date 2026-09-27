package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.service.support.RecipientRiskScoringService;
import com.aspire.asat.phishing.service.support.RecipientTrainingAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VoiceIngestionServiceImplTest {

    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String TRACKING_ID = "trk-1";

    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private VishingCallLogRepository callLogRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private RecipientTrainingAssignmentService recipientTrainingAssignmentService;
    @Mock
    private RecipientRiskScoringService recipientRiskScoringService;
    @Mock
    private CampaignCompletionEvaluator completionEvaluator;

    @InjectMocks
    private VoiceIngestionServiceImpl voiceIngestionService;

    private CampaignRecipient recipient;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        recipient = CampaignRecipient.builder()
                .id("recipient-1")
                .campaignId(CAMPAIGN_ID)
                .userId("user-1")
                .clientId("client-1")
                .email("voice.user@example.com")
                .phoneNumber("+15551234567")
                .trackingId(TRACKING_ID)
                .status(RecipientStatus.CALL_QUEUED)
                .build();

        campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .campaignType(CampaignType.VISHING_WITH_TRAINING)
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .stats(new CampaignStats())
                .build();

        when(recipientRepository.findByTrackingId(TRACKING_ID)).thenReturn(Optional.of(recipient));
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(callLogRepository.findByTrackingId(TRACKING_ID)).thenReturn(Optional.empty());
    }

    private void process(VishingCallOutcome outcome) {
        voiceIngestionService.processCallResult(TRACKING_ID,
                VoiceCallResultRequest.builder().outcome(outcome).build());
    }

    @Test
    void answeredScores20AndNoTraining() {
        process(VishingCallOutcome.ANSWERED);
        assertEquals(RecipientStatus.ANSWERED, recipient.getStatus());
        assertEquals(20.0, recipient.getRiskScore());
        verify(recipientTrainingAssignmentService, never()).assignTrainingSubPackageIfNeeded(any(), any());
    }

    @Test
    void engagedScores75AndTriggersClickTraining() {
        process(VishingCallOutcome.ENGAGED);
        assertEquals(RecipientStatus.VOICE_ENGAGED, recipient.getStatus());
        assertEquals(75.0, recipient.getRiskScore());
        verify(recipientTrainingAssignmentService)
                .assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.VOICE_ENGAGED);
    }

    @Test
    void compromisedScores100AndTriggersCompromiseTraining() {
        process(VishingCallOutcome.COMPROMISED);
        assertEquals(RecipientStatus.COMPROMISED, recipient.getStatus());
        assertEquals(100.0, recipient.getRiskScore());
        verify(recipientTrainingAssignmentService)
                .assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.COMPROMISED);
    }

    @Test
    void answeredButReportedScores10WithReportedStatus() {
        process(VishingCallOutcome.ANSWERED_BUT_REPORTED);
        assertEquals(RecipientStatus.REPORTED, recipient.getStatus());
        assertEquals(10.0, recipient.getRiskScore());
        verify(recipientTrainingAssignmentService, never()).assignTrainingSubPackageIfNeeded(any(), any());
    }

    @Test
    void reportedWithoutAnswerScores0WithReportedStatus() {
        process(VishingCallOutcome.REPORTED_WITHOUT_ANSWER);
        assertEquals(RecipientStatus.REPORTED, recipient.getStatus());
        assertEquals(0.0, recipient.getRiskScore());
    }

    @Test
    void noAnswerScores0() {
        process(VishingCallOutcome.NO_ANSWER);
        assertEquals(RecipientStatus.NO_ANSWER, recipient.getStatus());
        assertEquals(0.0, recipient.getRiskScore());
    }

    @Test
    void failedScores0() {
        process(VishingCallOutcome.FAILED);
        assertEquals(RecipientStatus.CALL_FAILED, recipient.getStatus());
        assertEquals(0.0, recipient.getRiskScore());
        verify(completionEvaluator).evaluateAndApply(any(Campaign.class));
    }

    @Test
    void retryDoesNotRegressAlreadyCompromisedRecipient() {
        when(callLogRepository.findByTrackingId(TRACKING_ID))
                .thenReturn(Optional.of(VishingCallLog.builder()
                        .trackingId(TRACKING_ID)
                        .outcome(VishingCallOutcome.COMPROMISED)
                        .build()));

        process(VishingCallOutcome.ANSWERED);

        // Recipient retains its prior state; the lower-severity retry is a no-op.
        assertEquals(RecipientStatus.CALL_QUEUED, recipient.getStatus());
        verify(recipientRepository, never()).save(any());
        verify(completionEvaluator, never()).evaluateAndApply(any(Campaign.class));
    }

    @Test
    void engagedIncrementsAnsweredAndEngagedStats() {
        process(VishingCallOutcome.ENGAGED);

        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(1, campaign.getStats().getCallsEngaged());
        assertEquals(0, campaign.getStats().getCallsCompromised());
    }

    @Test
    void compromisedIncrementsAnsweredEngagedAndCompromisedStats() {
        process(VishingCallOutcome.COMPROMISED);

        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(1, campaign.getStats().getCallsEngaged());
        assertEquals(1, campaign.getStats().getCallsCompromised());
    }

    @Test
    void noAnswerIncrementsOnlyNoAnswerStat() {
        process(VishingCallOutcome.NO_ANSWER);

        assertEquals(0, campaign.getStats().getCallsAnswered());
        assertEquals(0, campaign.getStats().getCallsEngaged());
        assertEquals(0, campaign.getStats().getCallsCompromised());
        assertEquals(1, campaign.getStats().getCallsNoAnswer());
    }

    @Test
    void upgradeAnsweredToEngagedIncrementsOnlyEngaged() {
        process(VishingCallOutcome.ANSWERED);
        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(0, campaign.getStats().getCallsEngaged());

        when(callLogRepository.findByTrackingId(TRACKING_ID))
                .thenReturn(Optional.of(VishingCallLog.builder()
                        .trackingId(TRACKING_ID)
                        .outcome(VishingCallOutcome.ANSWERED)
                        .build()));

        process(VishingCallOutcome.ENGAGED);

        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(1, campaign.getStats().getCallsEngaged());
        assertEquals(0, campaign.getStats().getCallsCompromised());
    }

    @Test
    void upgradeEngagedToCompromisedIncrementsOnlyCompromised() {
        process(VishingCallOutcome.ENGAGED);
        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(1, campaign.getStats().getCallsEngaged());

        when(callLogRepository.findByTrackingId(TRACKING_ID))
                .thenReturn(Optional.of(VishingCallLog.builder()
                        .trackingId(TRACKING_ID)
                        .outcome(VishingCallOutcome.ENGAGED)
                        .build()));

        process(VishingCallOutcome.COMPROMISED);

        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(1, campaign.getStats().getCallsEngaged());
        assertEquals(1, campaign.getStats().getCallsCompromised());
    }

    @Test
    void duplicateCompromisedDoesNotDoubleCountStats() {
        process(VishingCallOutcome.COMPROMISED);
        when(callLogRepository.findByTrackingId(TRACKING_ID))
                .thenReturn(Optional.of(VishingCallLog.builder()
                        .trackingId(TRACKING_ID)
                        .outcome(VishingCallOutcome.COMPROMISED)
                        .build()));

        process(VishingCallOutcome.COMPROMISED);

        assertEquals(1, campaign.getStats().getCallsAnswered());
        assertEquals(1, campaign.getStats().getCallsEngaged());
        assertEquals(1, campaign.getStats().getCallsCompromised());
    }

    @Test
    void processCallResult_StoresRecipientEmailOnActivityNotPhone() {
        process(VishingCallOutcome.ANSWERED);

        ArgumentCaptor<EmailActivity> activityCaptor = ArgumentCaptor.forClass(EmailActivity.class);
        verify(emailActivityRepository).save(activityCaptor.capture());
        EmailActivity activity = activityCaptor.getValue();
        assertEquals("voice.user@example.com", activity.getRecipientEmail());
        assertEquals("+15551234567", activity.getMetadata().get("phoneNumber"));
    }
}
