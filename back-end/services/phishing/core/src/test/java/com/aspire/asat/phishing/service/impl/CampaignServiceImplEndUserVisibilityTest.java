package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.EndUserPhishingVisibilityDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplEndUserVisibilityTest {

    private static final String CLIENT_ID = "client-1";
    private static final String USER_ID = "user-1";

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
    void hasReceivedPhishingCampaign_returnsFalseWhenNoRecipient() {
        stubEndUserContext();
        when(recipientRepository.findByClientIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(List.of());

        EndUserPhishingVisibilityDto result = campaignService.hasReceivedPhishingCampaign();

        assertFalse(result.isHasReceivedCampaign());
        assertFalse(result.isHasReceivedEmailCampaign());
        assertFalse(result.isHasReceivedSmsCampaign());
        assertFalse(result.isHasReceivedVoiceCampaign());
        verify(campaignRepository, never()).findByIdInAndClientId(anyList(), any());
    }

    @Test
    void hasReceivedPhishingCampaign_emailOnly_setsEmailFlag() {
        stubEndUserContext();
        stubRecipients("c-email");
        when(campaignRepository.findByIdInAndClientId(eq(List.of("c-email")), eq(CLIENT_ID)))
                .thenReturn(List.of(campaign("c-email", CampaignChannel.EMAIL)));

        EndUserPhishingVisibilityDto result = campaignService.hasReceivedPhishingCampaign();

        assertTrue(result.isHasReceivedCampaign());
        assertTrue(result.isHasReceivedEmailCampaign());
        assertFalse(result.isHasReceivedSmsCampaign());
        assertFalse(result.isHasReceivedVoiceCampaign());
    }

    @Test
    void hasReceivedPhishingCampaign_smsOnly_setsSmsFlag() {
        stubEndUserContext();
        stubRecipients("c-sms");
        when(campaignRepository.findByIdInAndClientId(eq(List.of("c-sms")), eq(CLIENT_ID)))
                .thenReturn(List.of(campaign("c-sms", CampaignChannel.SMS)));

        EndUserPhishingVisibilityDto result = campaignService.hasReceivedPhishingCampaign();

        assertTrue(result.isHasReceivedCampaign());
        assertFalse(result.isHasReceivedEmailCampaign());
        assertTrue(result.isHasReceivedSmsCampaign());
        assertFalse(result.isHasReceivedVoiceCampaign());
    }

    @Test
    void hasReceivedPhishingCampaign_voiceOnly_setsVoiceFlag() {
        stubEndUserContext();
        stubRecipients("c-voice");
        when(campaignRepository.findByIdInAndClientId(eq(List.of("c-voice")), eq(CLIENT_ID)))
                .thenReturn(List.of(campaign("c-voice", CampaignChannel.VOICE)));

        EndUserPhishingVisibilityDto result = campaignService.hasReceivedPhishingCampaign();

        assertTrue(result.isHasReceivedCampaign());
        assertFalse(result.isHasReceivedEmailCampaign());
        assertFalse(result.isHasReceivedSmsCampaign());
        assertTrue(result.isHasReceivedVoiceCampaign());
    }

    @Test
    void hasReceivedPhishingCampaign_mixedChannels_setsMatchingFlags() {
        stubEndUserContext();
        stubRecipients("c-email", "c-voice");
        when(campaignRepository.findByIdInAndClientId(eq(List.of("c-email", "c-voice")), eq(CLIENT_ID)))
                .thenReturn(List.of(
                        campaign("c-email", CampaignChannel.EMAIL),
                        campaign("c-voice", CampaignChannel.VOICE)));

        EndUserPhishingVisibilityDto result = campaignService.hasReceivedPhishingCampaign();

        assertTrue(result.isHasReceivedCampaign());
        assertTrue(result.isHasReceivedEmailCampaign());
        assertFalse(result.isHasReceivedSmsCampaign());
        assertTrue(result.isHasReceivedVoiceCampaign());
    }

    @Test
    void hasReceivedPhishingCampaign_nullChannel_countsAsEmail() {
        stubEndUserContext();
        stubRecipients("c-legacy");
        Campaign legacy = campaign("c-legacy", CampaignChannel.EMAIL);
        legacy.setChannel(null);
        when(campaignRepository.findByIdInAndClientId(eq(List.of("c-legacy")), eq(CLIENT_ID)))
                .thenReturn(List.of(legacy));

        EndUserPhishingVisibilityDto result = campaignService.hasReceivedPhishingCampaign();

        assertTrue(result.isHasReceivedCampaign());
        assertTrue(result.isHasReceivedEmailCampaign());
        assertFalse(result.isHasReceivedSmsCampaign());
        assertFalse(result.isHasReceivedVoiceCampaign());
    }

    @Test
    void hasReceivedPhishingCampaign_rejectsNonUserRole() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.CLIENT_ADMIN.name());
        context.setUserId(USER_ID);
        context.setClientAdminId(CLIENT_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        assertThrows(ServiceException.class, () -> campaignService.hasReceivedPhishingCampaign());
    }

    @Test
    void hasReceivedPhishingCampaign_rejectsMissingUserId() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.USER.name());
        context.setClientAdminId(CLIENT_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        assertThrows(ServiceException.class, () -> campaignService.hasReceivedPhishingCampaign());
    }

    private void stubEndUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.USER.name());
        context.setUserId(USER_ID);
        context.setClientAdminId(CLIENT_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
    }

    private void stubRecipients(String... campaignIds) {
        List<CampaignRecipient> recipients = Arrays.stream(campaignIds)
                .map(id -> CampaignRecipient.builder()
                        .campaignId(id)
                        .clientId(CLIENT_ID)
                        .userId(USER_ID)
                        .build())
                .toList();
        when(recipientRepository.findByClientIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(recipients);
    }

    private static Campaign campaign(String id, CampaignChannel channel) {
        return Campaign.builder()
                .id(id)
                .clientId(CLIENT_ID)
                .channel(channel)
                .build();
    }
}
