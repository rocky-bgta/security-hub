package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.RegistrationTimezone;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.ScheduleType;
import com.aspire.asat.phishing.dto.enums.SendingPattern;
import com.aspire.asat.phishing.dto.request.CampaignScheduleRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
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
import com.aspire.asat.phishing.util.CampaignScheduleDateTimeParser;
import com.aspire.asat.phishing.util.ScheduleDateTimeParseResult;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplScheduleTest {

    private static final String TIMEZONE_ID = "5f1e0b48-8691-4959-8978-13e6eb876cf7";
    private static final String EAT_DISPLAY = "EAT (UTC+03:00)";
    private static final String EAT_IANA = "Africa/Nairobi";

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
    private CampaignScheduleDateTimeParser scheduleDateTimeParser;
    @Mock
    private TrackingBaseUrlResolver trackingBaseUrlResolver;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    void updateScheduleResolvesTimeZoneIdAndPersistsDisplayName() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId("client-1").build());

        Campaign draft = Campaign.builder()
                .id("cmp-1")
                .clientId("client-1")
                .status(CampaignStatus.DRAFT)
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .build();
        when(campaignRepository.findByIdAndClientId("cmp-1", "client-1")).thenReturn(Optional.of(draft));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenReturn(null);
        when(registrationClient.getTimezoneByDocumentId(TIMEZONE_ID))
                .thenReturn(Optional.of(new RegistrationTimezone(EAT_DISPLAY, EAT_IANA)));

        Instant futureStart = Instant.now().plusSeconds(7200);
        CampaignScheduleRequest request = CampaignScheduleRequest.builder()
                .scheduleType(ScheduleType.SCHEDULED)
                .startDateTime("2026-12-01T10:00")
                .timezone(TIMEZONE_ID)
                .sendingPattern(SendingPattern.ALL_AT_ONCE)
                .build();

        when(scheduleDateTimeParser.parseSchedule(
                request.getStartDateTime(), request.getEndDateTime(), EAT_DISPLAY, EAT_IANA))
                .thenReturn(ScheduleDateTimeParseResult.builder()
                        .startDateTime(futureStart)
                        .endDateTime(null)
                        .build());

        campaignService.updateSchedule("cmp-1", request);

        verify(registrationClient).getTimezoneByDocumentId(TIMEZONE_ID);
        verify(scheduleDateTimeParser).parseSchedule(
                request.getStartDateTime(), request.getEndDateTime(), EAT_DISPLAY, EAT_IANA);
        verify(campaignMapper).applyScheduleRequest(
                eq(draft), eq(request), eq(futureStart), eq(null), eq(EAT_DISPLAY));
    }

    @Test
    void updateScheduleAcceptsUtcTimezoneIdLiteral() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId("client-1").build());

        Campaign draft = Campaign.builder()
                .id("cmp-1")
                .clientId("client-1")
                .status(CampaignStatus.DRAFT)
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .build();
        when(campaignRepository.findByIdAndClientId("cmp-1", "client-1")).thenReturn(Optional.of(draft));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenReturn(null);
        when(registrationClient.getTimezoneByTimezoneId("UTC")).thenReturn(Optional.empty());

        Instant futureStart = Instant.now().plusSeconds(7200);
        CampaignScheduleRequest request = CampaignScheduleRequest.builder()
                .scheduleType(ScheduleType.SCHEDULED)
                .startDateTime("2026-12-01T10:00")
                .timezone("UTC")
                .sendingPattern(SendingPattern.ALL_AT_ONCE)
                .build();

        when(scheduleDateTimeParser.parseSchedule(
                request.getStartDateTime(), request.getEndDateTime(), "UTC", "UTC"))
                .thenReturn(ScheduleDateTimeParseResult.builder()
                        .startDateTime(futureStart)
                        .endDateTime(null)
                        .build());

        campaignService.updateSchedule("cmp-1", request);

        verify(registrationClient, never()).getTimezoneByDocumentId(any());
        verify(registrationClient).getTimezoneByTimezoneId("UTC");
        verify(scheduleDateTimeParser).parseSchedule(
                request.getStartDateTime(), request.getEndDateTime(), "UTC", "UTC");
        verify(campaignMapper).applyScheduleRequest(
                eq(draft), eq(request), eq(futureStart), eq(null), eq("UTC"));
    }

    @Test
    void updateScheduleRejectsUnknownTimezoneDocumentId() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId("client-1").build());

        Campaign draft = Campaign.builder()
                .id("cmp-1")
                .clientId("client-1")
                .status(CampaignStatus.DRAFT)
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .build();
        when(campaignRepository.findByIdAndClientId("cmp-1", "client-1")).thenReturn(Optional.of(draft));
        when(registrationClient.getTimezoneByDocumentId(TIMEZONE_ID)).thenReturn(Optional.empty());

        CampaignScheduleRequest request = CampaignScheduleRequest.builder()
                .scheduleType(ScheduleType.SCHEDULED)
                .startDateTime("2026-12-01T10:00")
                .timezone(TIMEZONE_ID)
                .sendingPattern(SendingPattern.ALL_AT_ONCE)
                .build();

        PhishingValidationException ex = assertThrows(PhishingValidationException.class,
                () -> campaignService.updateSchedule("cmp-1", request));

        assertTrue(ex.getMessage().contains("Unable to resolve timezone id"));
        verify(registrationClient).getTimezoneByDocumentId(TIMEZONE_ID);
        verify(registrationClient, never()).getTimezoneByTimezoneId(any());
    }
}
