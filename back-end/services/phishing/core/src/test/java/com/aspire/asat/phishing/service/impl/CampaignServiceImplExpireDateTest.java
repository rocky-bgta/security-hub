package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignExpiryValidityUnit;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.request.CampaignCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignExpireDateRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignSchedule;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplExpireDateTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignMapper campaignMapper;
    @Mock
    private RegistrationServiceClient registrationClient;
    @Mock
    private CmsSubPackageClient cmsSubPackageClient;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private SenderProfileRepository senderProfileRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private UserRiskProfileService userRiskProfileService;
    @Mock
    private CampaignUserRiskProfileAsyncUpdater campaignUserRiskProfileAsyncUpdater;
    @Mock
    private TrackingBaseUrlResolver trackingBaseUrlResolver;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    void createCampaignShouldComputeExpiresAtWithDaysFromNow() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(campaignRepository.existsByClientIdAndCampaignName(anyString(), anyString())).thenReturn(false);

        Campaign mappedCampaign = Campaign.builder().campaignName("Q2").campaignType(CampaignType.SIMULATED_PHISHING).build();
        when(campaignMapper.toEntity(any(CampaignCreateRequest.class), anyString())).thenReturn(mappedCampaign);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenReturn(null);

        Instant before = Instant.now();
        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignName("Q2")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .expireDate(CampaignExpireDateRequest.builder()
                        .validityUnit(CampaignExpiryValidityUnit.DAYS)
                        .validityPeriod(31)
                        .build())
                .build();

        campaignService.createCampaign(request);
        Instant after = Instant.now();

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        Instant expectedMin = before.plus(31, ChronoUnit.DAYS);
        Instant expectedMax = after.plus(31, ChronoUnit.DAYS);
        assertEquals(request.getExpireDate(), captor.getValue().getExpireDate());
        assertTrue(!captor.getValue().getExpiresAt().isBefore(expectedMin)
                && !captor.getValue().getExpiresAt().isAfter(expectedMax));
    }

    @Test
    void createCampaignShouldComputeExpiresAtWithMonthsFromNow() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(campaignRepository.existsByClientIdAndCampaignName(anyString(), anyString())).thenReturn(false);

        Campaign mappedCampaign = Campaign.builder().campaignName("Q3").campaignType(CampaignType.SIMULATED_PHISHING).build();
        when(campaignMapper.toEntity(any(CampaignCreateRequest.class), anyString())).thenReturn(mappedCampaign);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenReturn(null);

        Instant before = Instant.now();
        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignName("Q3")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .expireDate(CampaignExpireDateRequest.builder()
                        .validityUnit(CampaignExpiryValidityUnit.MONTHS)
                        .validityPeriod(12)
                        .build())
                .build();

        campaignService.createCampaign(request);
        Instant after = Instant.now();

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        Instant expectedMin = before.plus(365, ChronoUnit.DAYS);
        Instant expectedMax = after.plus(366, ChronoUnit.DAYS);
        assertTrue(!captor.getValue().getExpiresAt().isBefore(expectedMin)
                && !captor.getValue().getExpiresAt().isAfter(expectedMax));
    }

    @Test
    void updateCampaignSetupShouldUseScheduleStartAsExpiryBaseWhenPresent() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(campaignRepository.existsByClientIdAndCampaignNameAndIdNot(anyString(), anyString(), anyString())).thenReturn(false);

        Instant scheduleStart = Instant.parse("2026-08-01T10:00:00Z");
        Campaign existing = Campaign.builder()
                .id("cmp-1")
                .clientId("client-1")
                .campaignName("Existing")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .schedule(CampaignSchedule.builder().startDateTime(scheduleStart).build())
                .build();

        when(campaignRepository.findByIdAndClientId("cmp-1", "client-1")).thenReturn(Optional.of(existing));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenReturn(null);

        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignName("Updated")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .expireDate(CampaignExpireDateRequest.builder()
                        .validityUnit(CampaignExpiryValidityUnit.DAYS)
                        .validityPeriod(31)
                        .build())
                .build();

        campaignService.updateCampaignSetup("cmp-1", request);

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        assertEquals(request.getExpireDate(), captor.getValue().getExpireDate());
        assertEquals(scheduleStart.plus(31, ChronoUnit.DAYS), captor.getValue().getExpiresAt());
    }

    @Test
    void createCampaignShouldRejectInvalidValidityPeriod() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(campaignRepository.existsByClientIdAndCampaignName(anyString(), anyString())).thenReturn(false);
        when(campaignMapper.toEntity(any(CampaignCreateRequest.class), anyString())).thenReturn(Campaign.builder().build());

        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignName("Invalid")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .expireDate(CampaignExpireDateRequest.builder()
                        .validityUnit(CampaignExpiryValidityUnit.DAYS)
                        .validityPeriod(0)
                        .build())
                .build();

        assertThrows(PhishingValidationException.class, () -> campaignService.createCampaign(request));
    }
}
