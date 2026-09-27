package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.CmsTopicClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.cms.ClientAdminInfoDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterRequestDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterResponseDto;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.dto.response.BrandDto;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.dto.response.TargetIndustryDto;
import com.aspire.asat.phishing.dto.response.ToneDto;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.service.TopicRecommendationFilterAssembler;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplRecommendedTopicsTest {

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
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private UserRiskProfileService userRiskProfileService;
    @Mock
    private CampaignUserRiskProfileAsyncUpdater campaignUserRiskProfileAsyncUpdater;
    @Mock
    private TrackingBaseUrlResolver trackingBaseUrlResolver;

    @Spy
    private TopicRecommendationFilterAssembler topicRecommendationFilterAssembler = new TopicRecommendationFilterAssembler();

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Captor
    private ArgumentCaptor<CmsTopicFilterRequestDto> filterRequestCaptor;

    @Test
    void getRecommendedTopics_buildsFullCatalogFilterFromEmailTemplate() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .emailTemplateId("template-1")
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));

        EmailTemplate template = EmailTemplate.builder()
                .id("template-1")
                .payloadType(PayloadTypeDto.builder().id("payload-001").name("Link").build())
                .difficultyLevel(DifficultyDto.builder().id("diff-001").name("Intermediate").build())
                .tone(ToneDto.builder().id("tone-001").name("Urgent").build())
                .socialEngineeringStrategy(SocialEngineeringStrategyDto.builder().id("ses-001").name("Authority").build())
                .triggerEvent(TriggerEventDto.builder().id("te-001").name("Password reset").build())
                .attackTechnique(AttackTechniqueDto.builder().id("at-001").name("Spear").build())
                .attackerPersona(AttackerPersonaDto.builder().id("ap-001").name("IT").build())
                .campaignObjective(CampaignObjectiveDto.builder().id("co-001").name("Credential").build())
                .emotionalTrigger(EmotionalTriggerDto.builder().id("et-001").name("Fear").build())
                .urgencyLevel(UrgencyLevelDto.builder().id("ul-001").name("High").build())
                .brand(BrandDto.builder().id("brand-001").name("Acme").build())
                .callToAction(CallToActionDto.builder().id("cta-001").name("Click").build())
                .tags(List.of("phishing"))
                .build();
        when(emailTemplateRepository.findById("template-1")).thenReturn(Optional.of(template));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(null);

        when(cmsTopicClient.recommendTopicsByProduct(eq("product-1"), any()))
                .thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", "product-1", null, 1, 20);

        verify(cmsTopicClient).recommendTopicsByProduct(eq("product-1"), filterRequestCaptor.capture());
        CmsTopicFilterRequestDto filter = filterRequestCaptor.getValue();

        assertEquals(List.of("payload-001"), filter.getPayloadTypeIds());
        assertEquals(List.of("diff-001"), filter.getDifficultyIds());
        assertEquals(List.of("tone-001"), filter.getToneIds());
        assertEquals(List.of("ses-001"), filter.getSocialEngineeringStrategyIds());
        assertEquals(List.of("te-001"), filter.getTriggerEventIds());
        assertEquals(List.of("at-001"), filter.getAttackTechniqueIds());
        assertEquals(List.of("ap-001"), filter.getAttackerPersonaIds());
        assertEquals(List.of("co-001"), filter.getCampaignObjectiveIds());
        assertEquals(List.of("et-001"), filter.getEmotionalTriggerIds());
        assertEquals(List.of("ul-001"), filter.getUrgencyLevelIds());
        assertEquals(List.of("brand-001"), filter.getBrandIds());
        assertEquals(List.of("cta-001"), filter.getCallToActionIds());
        assertEquals(List.of("phishing"), filter.getTags());
        assertEquals("ENABLED", filter.getStatus());
        assertEquals("client-1", filter.getClientId());
        assertEquals(1, filter.getPage());
        assertEquals(20, filter.getSize());
    }

    @Test
    void getRecommendedTopics_usesLandingPageDifficultyCatalogIdWhenTemplateHasNone() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .emailTemplateId("template-1")
                .landingPageId("lp-1")
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));

        when(emailTemplateRepository.findById("template-1")).thenReturn(Optional.of(EmailTemplate.builder().id("template-1").build()));

        LandingPage landingPage = LandingPage.builder()
                .id("lp-1")
                .difficultyLevel(DifficultyDto.builder().id("diff-catalog-1").name("Hard").build())
                .build();
        when(landingPageRepository.findById("lp-1")).thenReturn(Optional.of(landingPage));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(null);
        when(cmsTopicClient.recommendTopics(any())).thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopics(filterRequestCaptor.capture());
        assertEquals(List.of("diff-catalog-1"), filterRequestCaptor.getValue().getDifficultyIds());
    }

    @Test
    void getRecommendedTopics_usesLandingUrgencyAndEmotionalWhenTemplateMissing() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .emailTemplateId("template-1")
                .landingPageId("lp-1")
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));
        when(emailTemplateRepository.findById("template-1")).thenReturn(Optional.of(EmailTemplate.builder().id("template-1").build()));

        LandingPage landingPage = LandingPage.builder()
                .id("lp-1")
                .urgencyLevel(UrgencyLevelDto.builder().id("ul-lp").name("Medium").build())
                .emotionalTrigger(EmotionalTriggerDto.builder().id("et-lp").name("Curiosity").build())
                .build();
        when(landingPageRepository.findById("lp-1")).thenReturn(Optional.of(landingPage));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(null);
        when(cmsTopicClient.recommendTopics(any())).thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopics(filterRequestCaptor.capture());
        CmsTopicFilterRequestDto filter = filterRequestCaptor.getValue();
        assertEquals(List.of("ul-lp"), filter.getUrgencyLevelIds());
        assertEquals(List.of("et-lp"), filter.getEmotionalTriggerIds());
    }

    @Test
    void getRecommendedTopics_industryPrioritySenderOverTemplateOverAdmin() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .emailTemplateId("template-1")
                .senderProfileId("sender-1")
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));

        EmailTemplate template = EmailTemplate.builder()
                .id("template-1")
                .targetIndustry(TargetIndustryDto.builder().id("ind-template").name("Finance").build())
                .build();
        when(emailTemplateRepository.findById("template-1")).thenReturn(Optional.of(template));
        when(senderProfileRepository.findById("sender-1")).thenReturn(Optional.of(
                SenderProfile.builder().id("sender-1").targetIndustryId("ind-sender").build()));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(
                ClientAdminInfoDto.builder().industry("ind-admin").build());
        when(cmsTopicClient.recommendTopics(any())).thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopics(filterRequestCaptor.capture());
        assertEquals(List.of("ind-sender"), filterRequestCaptor.getValue().getIndustryIds());
    }

    @Test
    void getRecommendedTopics_industryFallsBackToTemplateWhenSenderMissing() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .emailTemplateId("template-1")
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));

        EmailTemplate template = EmailTemplate.builder()
                .id("template-1")
                .targetIndustry(TargetIndustryDto.builder().id("ind-template").name("Finance").build())
                .build();
        when(emailTemplateRepository.findById("template-1")).thenReturn(Optional.of(template));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(
                ClientAdminInfoDto.builder().industry("ind-admin").build());
        when(cmsTopicClient.recommendTopics(any())).thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopics(filterRequestCaptor.capture());
        assertEquals(List.of("ind-template"), filterRequestCaptor.getValue().getIndustryIds());
    }

    @Test
    void getRecommendedTopics_populatesAdminTenantFilters() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder().id("campaign-1").clientId("client-1").build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(ClientAdminInfoDto.builder()
                .country("country-001")
                .complianceId("pci-001")
                .subIndustryId("sub-001")
                .build());
        when(cmsTopicClient.recommendTopics(any())).thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopics(filterRequestCaptor.capture());
        CmsTopicFilterRequestDto filter = filterRequestCaptor.getValue();
        assertEquals(List.of("country-001"), filter.getCountryIds());
        assertEquals(List.of("pci-001"), filter.getComplianceIds());
        assertEquals(List.of("sub-001"), filter.getSubIndustryIds());
        assertNull(filter.getIndustryIds());
    }

    @Test
    void getRecommendedTopics_resolvesProductIdFromCampaignTrainingData() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .trainingData(CampaignTrainingData.builder()
                        .productId("product-from-campaign")
                        .packageId("package-from-campaign")
                        .build())
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(null);
        when(cmsTopicClient.recommendTopicsByProduct(eq("product-from-campaign"), any()))
                .thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopicsByProduct(eq("product-from-campaign"), any());
        verify(cmsTopicClient, never()).recommendTopicsByPackage(any(), any());
        verify(cmsTopicClient, never()).recommendTopics(any());
    }

    @Test
    void getRecommendedTopics_usesUnscopedFilterWhenNoProductOrPackageAvailable() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId("client-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        Campaign campaign = Campaign.builder()
                .id("campaign-1")
                .clientId("client-1")
                .build();
        when(campaignRepository.findByIdAndClientId("campaign-1", "client-1"))
                .thenReturn(Optional.of(campaign));
        when(registrationClient.getClientAdminInfo("client-1")).thenReturn(null);
        when(cmsTopicClient.recommendTopics(any())).thenReturn(new CmsTopicFilterResponseDto());

        campaignService.getRecommendedTopics("campaign-1", null, null, 1, 20);

        verify(cmsTopicClient).recommendTopics(any());
        verify(cmsTopicClient, never()).recommendTopicsByProduct(any(), any());
        verify(cmsTopicClient, never()).recommendTopicsByPackage(any(), any());
    }
}
