package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.CmsTopicClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.CampaignRiskImpactDto;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.util.CampaignScheduleDateTimeParser;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplRiskImpactFilterTest {

    private static final String CLIENT_ID = "client-1";

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
    private CmsTopicClient cmsTopicClient;
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
    private SmsServerConfigurationRepository smsServerConfigurationRepository;
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

    @BeforeEach
    void stubClientContext() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    @Test
    void getCampaignRiskImpact_passesSearchNameOrTypeAndFiltersToRepository() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-31T23:59:59Z");
        Campaign campaign = baseCampaign("c1", "Password Reset", CampaignType.SIMULATED_PHISHING, CampaignStatus.RUNNING);
        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID),
                eq("Password"),
                eq(List.of(CampaignStatus.RUNNING)),
                eq(CampaignChannel.EMAIL),
                eq(CampaignType.SIMULATED_PHISHING),
                eq(start),
                eq(end),
                eq(true),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign), org.springframework.data.domain.PageRequest.of(0, 10), 1));
        when(recipientRepository.findByCampaignId("c1")).thenReturn(List.of());

        Page<CampaignRiskImpactDto> page = campaignService.getCampaignRiskImpact(
                0, 10, "Password", CampaignType.SIMULATED_PHISHING, CampaignStatus.RUNNING, null, start, end, null);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, page.getTotalElements());
        assertEquals("Password Reset", page.getContent().get(0).getCampaignName());
        verify(campaignRepository).findWithStatuses(
                eq(CLIENT_ID),
                eq("Password"),
                eq(List.of(CampaignStatus.RUNNING)),
                eq(CampaignChannel.EMAIL),
                eq(CampaignType.SIMULATED_PHISHING),
                eq(start),
                eq(end),
                eq(true),
                any(Pageable.class));
    }

    @Test
    void getCampaignRiskImpact_getCampaigns_stillUsesNameOnlySearch() {
        when(campaignRepository.findWithFilters(
                eq(CLIENT_ID), eq("phish"), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        campaignService.getCampaigns(0, 10, "phish", null, null, "createdAt", "desc");

        verify(campaignRepository).findWithFilters(
                eq(CLIENT_ID), eq("phish"), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class));
    }

    @Test
    void getCampaignRiskImpact_riskImpactFilter_paginatesAfterCompute() {
        Campaign medium = baseCampaign("c-medium", "Medium Camp", CampaignType.SIMULATED_PHISHING, CampaignStatus.COMPLETED);
        Campaign high = baseCampaign("c-high", "High Camp", CampaignType.PHISHING_WITH_TRAINING, CampaignStatus.COMPLETED);
        Campaign low = baseCampaign("c-low", "Low Camp", CampaignType.SIMULATED_PHISHING, CampaignStatus.DRAFT);

        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID), isNull(), anyList(), eq(CampaignChannel.EMAIL),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(medium, high, low)));

        when(recipientRepository.findByCampaignId("c-medium")).thenReturn(List.of(
                CampaignRecipient.builder().campaignId("c-medium").userId("u-m").build()));
        when(recipientRepository.findByCampaignId("c-high")).thenReturn(List.of(
                CampaignRecipient.builder().campaignId("c-high").userId("u-h").build()));
        when(recipientRepository.findByCampaignId("c-low")).thenReturn(List.of(
                CampaignRecipient.builder().campaignId("c-low").userId("u-l").build()));
        when(userRiskProfileRepository.findByClientIdAndUserIdIn(eq(CLIENT_ID), anyList()))
                .thenAnswer(invocation -> profilesFor(invocation.getArgument(1)));

        Page<CampaignRiskImpactDto> page = campaignService.getCampaignRiskImpact(
                0, 10, null, null, null, RiskLevel.HIGH, null, null, CampaignChannel.EMAIL);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, page.getTotalElements());
        assertEquals("High Camp", page.getContent().get(0).getCampaignName());
        assertEquals(RiskLevel.HIGH, page.getContent().get(0).getRiskImpact());
    }

    @Test
    void getCampaignRiskImpact_riskImpactFilter_appliesPageSlice() {
        Campaign first = baseCampaign("c1", "A", CampaignType.SIMULATED_PHISHING, CampaignStatus.RUNNING);
        Campaign second = baseCampaign("c2", "B", CampaignType.SIMULATED_PHISHING, CampaignStatus.RUNNING);
        Campaign third = baseCampaign("c3", "C", CampaignType.SIMULATED_PHISHING, CampaignStatus.RUNNING);

        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID), isNull(), anyList(), eq(CampaignChannel.EMAIL),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second, third)));

        when(recipientRepository.findByCampaignId("c1")).thenReturn(List.of(
                CampaignRecipient.builder().campaignId("c1").userId("u1").build()));
        when(recipientRepository.findByCampaignId("c2")).thenReturn(List.of(
                CampaignRecipient.builder().campaignId("c2").userId("u2").build()));
        when(recipientRepository.findByCampaignId("c3")).thenReturn(List.of(
                CampaignRecipient.builder().campaignId("c3").userId("u3").build()));
        when(userRiskProfileRepository.findByClientIdAndUserIdIn(eq(CLIENT_ID), anyList()))
                .thenAnswer(invocation -> profilesFor(invocation.getArgument(1)));

        Page<CampaignRiskImpactDto> page = campaignService.getCampaignRiskImpact(
                1, 1, null, null, null, RiskLevel.MEDIUM, null, null, CampaignChannel.EMAIL);

        assertEquals(1, page.getContent().size());
        assertEquals(3L, page.getTotalElements());
        assertEquals("B", page.getContent().get(0).getCampaignName());
        assertTrue(page.getTotalPages() >= 3);
    }

    @Test
    void getCampaignRiskImpact_passesChannelToRepository() {
        Campaign campaign = baseCampaign("c-sms", "SMS Camp", CampaignType.SMISHING_WITH_TRAINING, CampaignStatus.RUNNING);
        campaign.setChannel(CampaignChannel.SMS);
        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID), isNull(), anyList(), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign), org.springframework.data.domain.PageRequest.of(0, 10), 1));
        when(recipientRepository.findByCampaignId("c-sms")).thenReturn(List.of());

        Page<CampaignRiskImpactDto> page = campaignService.getCampaignRiskImpact(
                0, 10, null, null, null, null, null, null, CampaignChannel.SMS);

        assertEquals(1, page.getContent().size());
        assertEquals("SMS Camp", page.getContent().get(0).getCampaignName());
        verify(campaignRepository).findWithStatuses(
                eq(CLIENT_ID), isNull(), anyList(), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class));
        verify(campaignRepository, org.mockito.Mockito.never()).findWithStatuses(
                eq(CLIENT_ID), isNull(), anyList(), eq(CampaignChannel.EMAIL),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class));
    }

    @Test
    void getCampaignRiskImpact_excludesDraftWhenStatusOmitted() {
        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID), isNull(), anyList(), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        campaignService.getCampaignRiskImpact(
                0, 10, null, null, null, null, null, null, CampaignChannel.SMS);

        ArgumentCaptor<List> statusesCaptor = ArgumentCaptor.forClass(List.class);
        verify(campaignRepository).findWithStatuses(
                eq(CLIENT_ID), isNull(), statusesCaptor.capture(), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class));
        assertTrue(statusesCaptor.getValue().stream().noneMatch(status -> status == CampaignStatus.DRAFT));
        assertTrue(statusesCaptor.getValue().contains(CampaignStatus.RUNNING));
        assertTrue(statusesCaptor.getValue().contains(CampaignStatus.COMPLETED));
        assertTrue(statusesCaptor.getValue().contains(CampaignStatus.EXPIRED));
    }

    @Test
    void getCampaignRiskImpact_honorsExplicitDraftStatusFilter() {
        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID), isNull(), eq(List.of(CampaignStatus.DRAFT)), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        campaignService.getCampaignRiskImpact(
                0, 10, null, null, CampaignStatus.DRAFT, null, null, null, CampaignChannel.SMS);

        verify(campaignRepository).findWithStatuses(
                eq(CLIENT_ID), isNull(), eq(List.of(CampaignStatus.DRAFT)), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(true), any(Pageable.class));
    }

    private Campaign baseCampaign(String id, String name, CampaignType type, CampaignStatus status) {
        return Campaign.builder()
                .id(id)
                .clientId(CLIENT_ID)
                .campaignName(name)
                .campaignType(type)
                .status(status)
                .createdAt(Instant.parse("2026-01-15T12:00:00Z"))
                .build();
    }

    private List<UserRiskProfile> profilesFor(List<String> userIds) {
        return userIds.stream()
                .map(userId -> UserRiskProfile.builder()
                        .clientId(CLIENT_ID)
                        .userId(userId)
                        .riskLevel(switch (userId) {
                            case "u-h" -> RiskLevel.HIGH;
                            case "u-l" -> RiskLevel.LOW;
                            default -> RiskLevel.MEDIUM;
                        })
                        .build())
                .toList();
    }
}
