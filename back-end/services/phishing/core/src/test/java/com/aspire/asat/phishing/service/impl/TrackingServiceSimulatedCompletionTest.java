package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.support.RecipientRiskScoringService;
import com.aspire.asat.phishing.service.support.RecipientTrainingAssignmentService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceSimulatedCompletionTest {

    private static final String CAMPAIGN_ID = "campaign-1";

    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private RegistrationServiceClient registrationServiceClient;
    @Mock
    private TrackingBaseUrlResolver trackingBaseUrlResolver;
    @Mock
    private CampaignCompletionEvaluator completionEvaluator;
    @Mock
    private RecipientTrainingAssignmentService recipientTrainingAssignmentService;
    @Mock
    private RecipientRiskScoringService recipientRiskScoringService;

    @InjectMocks
    private TrackingServiceImpl trackingService;

    @Test
    void recordOpenShouldInvokeCompletionEvaluatorForSimulatedCampaign() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("r1")
                .campaignId(CAMPAIGN_ID)
                .status(RecipientStatus.SENT)
                .trackingId("trk-1")
                .build();
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .campaignName("Simulated Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .stats(new CampaignStats())
                .build();

        when(recipientRepository.findByTrackingId("trk-1")).thenReturn(Optional.of(recipient));
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));

        trackingService.recordOpen("trk-1", "UA", "127.0.0.1");

        verify(completionEvaluator).evaluateAndApply(campaign);
        verify(campaignRepository).save(campaign);
    }
}
