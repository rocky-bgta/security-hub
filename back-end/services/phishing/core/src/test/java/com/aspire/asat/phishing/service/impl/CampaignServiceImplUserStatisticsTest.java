package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.response.UserCampaignStatisticsDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
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
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplUserStatisticsTest {

    private static final String CLIENT_ID = "client-1";
    private static final String USER_ID = "user-1";
    private static final List<String> EMAIL_CAMPAIGN_IDS = List.of("c-email");
    private static final List<String> SMS_CAMPAIGN_IDS = List.of("c-sms");
    private static final List<String> VOICE_CAMPAIGN_IDS = List.of("c-voice");
    private static final List<RecipientStatus> EMAIL_OPEN_STATUSES = List.of(
            RecipientStatus.OPENED, RecipientStatus.CLICKED,
            RecipientStatus.DATA_SUBMITTED, RecipientStatus.REPORTED);
    private static final List<RecipientStatus> EMAIL_CLICK_STATUSES = List.of(
            RecipientStatus.CLICKED, RecipientStatus.DATA_SUBMITTED);
    private static final List<RecipientStatus> VOICE_OPEN_STATUSES = List.of(
            RecipientStatus.ANSWERED, RecipientStatus.VOICE_ENGAGED, RecipientStatus.COMPROMISED);
    private static final List<RecipientStatus> VOICE_CLICK_STATUSES = List.of(
            RecipientStatus.VOICE_ENGAGED, RecipientStatus.COMPROMISED);

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
    void getUserCampaignStatistics_email_scopesToEmailCampaigns() {
        stubEndUserContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(EMAIL_CAMPAIGN_IDS);
        stubRecipientCounts(EMAIL_CAMPAIGN_IDS, EMAIL_OPEN_STATUSES, EMAIL_CLICK_STATUSES,
                RecipientStatus.DATA_SUBMITTED, 4L, 3L, 2L, 1L, 1L);

        UserCampaignStatisticsDto result = campaignService.getUserCampaignStatistics(CampaignChannel.EMAIL);

        assertEquals(4L, result.getTotalCampaigns());
        assertEquals(3L, result.getOpenCount());
        assertEquals(2L, result.getClickCount());
        assertEquals(1L, result.getCompromiseCount());
        assertEquals(1L, result.getReportCount());
        verify(userRiskProfileRepository, never()).findByClientIdAndUserId(any(), any());
    }

    @Test
    void getUserCampaignStatistics_nullChannel_defaultsToEmail() {
        stubEndUserContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(EMAIL_CAMPAIGN_IDS);
        stubRecipientCounts(EMAIL_CAMPAIGN_IDS, EMAIL_OPEN_STATUSES, EMAIL_CLICK_STATUSES,
                RecipientStatus.DATA_SUBMITTED, 2L, 1L, 1L, 0L, 0L);

        UserCampaignStatisticsDto result = campaignService.getUserCampaignStatistics(null);

        assertEquals(2L, result.getTotalCampaigns());
        verify(campaignRepository).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL);
    }

    @Test
    void getUserCampaignStatistics_sms_usesEmailStatusSet() {
        stubEndUserContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(SMS_CAMPAIGN_IDS);
        stubRecipientCounts(SMS_CAMPAIGN_IDS, EMAIL_OPEN_STATUSES, EMAIL_CLICK_STATUSES,
                RecipientStatus.DATA_SUBMITTED, 5L, 0L, 3L, 2L, 0L);

        UserCampaignStatisticsDto result = campaignService.getUserCampaignStatistics(CampaignChannel.SMS);

        assertEquals(5L, result.getTotalCampaigns());
        assertEquals(0L, result.getOpenCount());
        assertEquals(3L, result.getClickCount());
        assertEquals(2L, result.getCompromiseCount());
        assertEquals(0L, result.getReportCount());
    }

    @Test
    void getUserCampaignStatistics_voice_usesVoiceStatusSet() {
        stubEndUserContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.VOICE))
                .thenReturn(VOICE_CAMPAIGN_IDS);
        stubRecipientCounts(VOICE_CAMPAIGN_IDS, VOICE_OPEN_STATUSES, VOICE_CLICK_STATUSES,
                RecipientStatus.COMPROMISED, 3L, 2L, 1L, 1L, 1L);

        UserCampaignStatisticsDto result = campaignService.getUserCampaignStatistics(CampaignChannel.VOICE);

        assertEquals(3L, result.getTotalCampaigns());
        assertEquals(2L, result.getOpenCount());
        assertEquals(1L, result.getClickCount());
        assertEquals(1L, result.getCompromiseCount());
        assertEquals(1L, result.getReportCount());
        verify(recipientRepository, never()).countByClientIdAndUserIdAndCampaignIdInAndStatusIn(
                eq(CLIENT_ID), eq(USER_ID), eq(VOICE_CAMPAIGN_IDS), eq(EMAIL_OPEN_STATUSES));
    }

    @Test
    void getUserCampaignStatistics_emptyCampaignIds_returnsZeros() {
        stubEndUserContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of());

        UserCampaignStatisticsDto result = campaignService.getUserCampaignStatistics(CampaignChannel.EMAIL);

        assertEquals(0L, result.getTotalCampaigns());
        assertEquals(0L, result.getOpenCount());
        assertEquals(0L, result.getClickCount());
        assertEquals(0L, result.getCompromiseCount());
        assertEquals(0L, result.getReportCount());
        verify(recipientRepository, never()).countByClientIdAndUserIdAndCampaignIdIn(any(), any(), anyCollection());
    }

    @Test
    void getUserCampaignStatistics_rejectsNonUserRole() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.CLIENT_ADMIN.name());
        context.setUserId(USER_ID);
        context.setClientAdminId(CLIENT_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        assertThrows(ServiceException.class,
                () -> campaignService.getUserCampaignStatistics(CampaignChannel.EMAIL));
    }

    private void stubEndUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.USER.name());
        context.setUserId(USER_ID);
        context.setClientAdminId(CLIENT_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
    }

    private void stubRecipientCounts(
            List<String> campaignIds,
            List<RecipientStatus> openStatuses,
            List<RecipientStatus> clickStatuses,
            RecipientStatus compromiseStatus,
            long total,
            long opened,
            long clicked,
            long compromised,
            long reported) {
        when(recipientRepository.countByClientIdAndUserIdAndCampaignIdIn(CLIENT_ID, USER_ID, campaignIds))
                .thenReturn(total);
        when(recipientRepository.countByClientIdAndUserIdAndCampaignIdInAndStatusIn(
                CLIENT_ID, USER_ID, campaignIds, openStatuses)).thenReturn(opened);
        when(recipientRepository.countByClientIdAndUserIdAndCampaignIdInAndStatusIn(
                CLIENT_ID, USER_ID, campaignIds, clickStatuses)).thenReturn(clicked);
        when(recipientRepository.countByClientIdAndUserIdAndCampaignIdInAndStatus(
                CLIENT_ID, USER_ID, campaignIds, compromiseStatus)).thenReturn(compromised);
        when(recipientRepository.countByClientIdAndUserIdAndCampaignIdInAndStatus(
                CLIENT_ID, USER_ID, campaignIds, RecipientStatus.REPORTED)).thenReturn(reported);
    }
}
