package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceExpiryTest {

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

    @InjectMocks
    private TrackingServiceImpl trackingService;

    @Test
    void recordOpenShouldSkipAllMutationsWhenCampaignExpired() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("r1")
                .campaignId("c1")
                .status(RecipientStatus.SENT)
                .trackingId("trk-1")
                .build();
        Campaign campaign = Campaign.builder()
                .id("c1")
                .campaignName("Expired Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().minusSeconds(120))
                .build();

        when(recipientRepository.findByTrackingId("trk-1")).thenReturn(Optional.of(recipient));
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(campaign));

        trackingService.recordOpen("trk-1", "UA", "127.0.0.1");

        verify(recipientRepository, never()).save(any(CampaignRecipient.class));
        verify(emailActivityRepository, never()).save(any());
    }

    @Test
    void recordSubmissionShouldReturnRedirectButSkipMutationsWhenCampaignExpired() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("r1")
                .campaignId("c1")
                .status(RecipientStatus.CLICKED)
                .trackingId("trk-1")
                .build();
        Campaign campaign = Campaign.builder()
                .id("c1")
                .campaignName("Expired Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.RUNNING)
                .landingPageId("lp-1")
                .expiresAt(Instant.now().minusSeconds(120))
                .build();
        LandingPage landingPage = LandingPage.builder()
                .id("lp-1")
                .redirectUrl("https://example.com/after-submit")
                .build();

        when(recipientRepository.findByTrackingId("trk-1")).thenReturn(Optional.of(recipient));
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(campaign));
        when(landingPageRepository.findById("lp-1")).thenReturn(Optional.of(landingPage));

        String redirectUrl = trackingService.recordSubmission("trk-1", Map.of("u", "a"), "UA", "127.0.0.1");

        Assertions.assertEquals("https://example.com/after-submit", redirectUrl);
        verify(recipientRepository, never()).save(any(CampaignRecipient.class));
        verify(emailActivityRepository, never()).save(any());
        verify(userRiskProfileRepository, never()).save(any());
        verify(registrationServiceClient, never()).assignSubPackageToUser(any());
    }
}
