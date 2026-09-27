package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.PhishProneBucketTier;
import com.aspire.asat.phishing.dto.response.AdminDashboardUiDto;
import com.aspire.asat.phishing.dto.response.UserRiskDistributionDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.VishingScenarioRepository;
import com.aspire.asat.phishing.repository.custom.RecipientWindowActivityMetrics;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplPhishProneDataTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private SenderProfileRepository senderProfileRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private AdminDashboardAggregateRepository adminDashboardAggregateRepository;
    @Mock
    private DashboardStatsRepository dashboardStatsRepository;
    @Mock
    private DashboardAggregationService dashboardAggregationService;
    @Mock
    private DashboardCampaignRollupService campaignRollupService;
    @Mock
    private RegistrationServiceClient registrationServiceClient;
    @Mock
    private VishingScenarioRepository vishingScenarioRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getAdminUiDataShouldDefaultToThirtyDayWindow() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of("camp-email"));
        RecipientWindowActivityMetrics row = new RecipientWindowActivityMetrics(
                "user@asat.com", 10, 2, 4, 1, 1);
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-email")), any()))
                .thenReturn(List.of(row))
                .thenReturn(List.of());
        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), any()))
                .thenReturn(List.of(UserRiskProfile.builder()
                        .clientId(CLIENT_ID)
                        .userId("user-1")
                        .email("user@asat.com")
                        .build()));

        AdminDashboardUiDto result = dashboardService.getAdminUiData(30);

        assertEquals(1, result.getPhishProneUsers().stream()
                .filter(t -> t.getTier() == PhishProneBucketTier.CRITICAL)
                .findFirst()
                .orElseThrow()
                .getCount());
        assertEquals(1, result.getInformationSubmits());

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(emailActivityRepository, times(2)).aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), startCaptor.capture(), any(), eq(List.of("camp-email")), any());
        Instant currentWindowStart = startCaptor.getAllValues().get(0);
        long daysBetween = ChronoUnit.DAYS.between(currentWindowStart, Instant.now());
        assertEquals(30, daysBetween, 1);
        verify(userRiskProfileRepository, never()).findByClientId(any());
    }

    @Test
    void getAdminUiDataShouldScopeToSmsCampaignsAndActivityTypes() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms"));
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms")), any()))
                .thenReturn(List.of());

        AdminDashboardUiDto result = dashboardService.getAdminUiData(30, CampaignChannel.SMS);

        assertEquals(0, result.getHumanRiskScore().getScore());
        ArgumentCaptor<DashboardChannelScope.ActivityMapping> mappingCaptor =
                ArgumentCaptor.forClass(DashboardChannelScope.ActivityMapping.class);
        verify(emailActivityRepository).aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms")), mappingCaptor.capture());
        assertEquals(ActivityType.SMS_SENT, mappingCaptor.getValue().sent());
        verify(campaignRepository, never()).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL);
        verify(campaignRepository, never()).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.VOICE);
    }

    @Test
    void getAdminUiDataShouldUseSevenDayWindowWhenRequested() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of("camp-email"));
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-email")), any()))
                .thenReturn(List.of());

        AdminDashboardUiDto result = dashboardService.getAdminUiData(7);

        assertEquals(0, result.getHumanRiskScore().getScore());
        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(emailActivityRepository).aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), startCaptor.capture(), any(), eq(List.of("camp-email")), any());
        long daysBetween = ChronoUnit.DAYS.between(startCaptor.getValue(), Instant.now());
        assertEquals(7, daysBetween, 1);
    }

    @Test
    void getAdminUiDataShouldRejectInvalidDays() {
        assertThrows(PhishingValidationException.class, () -> dashboardService.getAdminUiData(15));
    }

    @Test
    void getUserRiskDistributionShouldComputeFromChannelScopedWindow() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.VOICE))
                .thenReturn(List.of("camp-voice"));
        RecipientWindowActivityMetrics row = new RecipientWindowActivityMetrics(
                "voice@asat.com", 4, 2, 1, 1, 0);
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-voice")), any()))
                .thenReturn(List.of(row));
        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), any()))
                .thenReturn(List.of(UserRiskProfile.builder()
                        .clientId(CLIENT_ID)
                        .userId("voice-user")
                        .email("voice@asat.com")
                        .build()));

        UserRiskDistributionDto result = dashboardService.getUserRiskDistribution(CampaignChannel.VOICE);

        assertEquals(1, result.getTotalUsers());
        assertEquals(1, result.getCompromisedUsers());
        ArgumentCaptor<DashboardChannelScope.ActivityMapping> mappingCaptor =
                ArgumentCaptor.forClass(DashboardChannelScope.ActivityMapping.class);
        verify(emailActivityRepository).aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-voice")), mappingCaptor.capture());
        assertEquals(ActivityType.VOICE_INITIATED, mappingCaptor.getValue().sent());
        assertEquals(ActivityType.VOICE_COMPROMISED, mappingCaptor.getValue().hack());
        verify(userRiskProfileRepository, never()).findByClientId(any());
    }

    @Test
    void getUserRiskDistributionShouldDefaultToEmail() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of());
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of()), any()))
                .thenReturn(List.of());

        UserRiskDistributionDto result = dashboardService.getUserRiskDistribution();

        assertEquals(0, result.getTotalUsers());
        verify(campaignRepository).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL);
    }

    @Test
    void getUserRiskDistribution_SmsChannel_ExcludesRowsWithNullUserId() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms-1"));

        RecipientWindowActivityMetrics user2 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u2@yopmail.com", 0, 0, 2, 1, 0);
        RecipientWindowActivityMetrics user3 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u3@yopmail.com", 0, 0, 1, 0, 0);
        RecipientWindowActivityMetrics user1 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u1@yopmail.com", 0, 0, 1, 0, 0);
        RecipientWindowActivityMetrics phone = new RecipientWindowActivityMetrics(
                "+8801773126589", 6, 0, 0, 0, 0);
        RecipientWindowActivityMetrics phoneVariant = new RecipientWindowActivityMetrics(
                "+88001773126589", 0, 0, 0, 0, 0);

        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms-1")), any()))
                .thenReturn(List.of(user2, user3, user1, phone, phoneVariant));
        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), any()))
                .thenReturn(List.of(
                        UserRiskProfile.builder()
                                .clientId(CLIENT_ID)
                                .userId("0fd55411-e8fc-442a-9079-d1f9514e9826")
                                .email("xtrail-dev-portal-u2@yopmail.com")
                                .build(),
                        UserRiskProfile.builder()
                                .clientId(CLIENT_ID)
                                .userId("65dabd5e-b884-427b-bda4-ce0acc026ab6")
                                .email("xtrail-dev-portal-u3@yopmail.com")
                                .build(),
                        UserRiskProfile.builder()
                                .clientId(CLIENT_ID)
                                .userId("f76f6b0d-3016-4bba-97ea-6192bb1b3f09")
                                .email("xtrail-dev-portal-u1@yopmail.com")
                                .build()));

        UserRiskDistributionDto result = dashboardService.getUserRiskDistribution(CampaignChannel.SMS);

        assertEquals(3, result.getTotalUsers());
        assertEquals(3, result.getCriticalRiskCount());
        assertEquals(0, result.getLowRiskCount());
        assertEquals(1, result.getCompromisedUsers());
        assertEquals(100.0, result.getCriticalRiskPercentage());
    }

    private void stubClientContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }
}
