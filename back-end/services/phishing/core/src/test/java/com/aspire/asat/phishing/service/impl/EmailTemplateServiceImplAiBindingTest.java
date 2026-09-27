package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.secret.AiSecretStoreService;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import com.aspire.asat.phishing.mapper.EmailTemplateMapper;
import com.aspire.asat.phishing.model.AiGenerationJob;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.repository.AiGenerationJobRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.service.AiContentGenerationSqsService;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import com.aspire.asat.phishing.service.PayloadTypeService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceImplAiBindingTest {

    private static final String CLIENT_ID = "user-1";
    private static final String TEMPLATE_ID = "template-ai-1";
    private static final String PAGE_ID = "page-1";

    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private EmailTemplateMapper emailTemplateMapper;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private AiGenerationJobRepository aiGenerationJobRepository;
    @Mock
    private AiContentGenerationSqsService aiContentGenerationSqsService;
    @Mock
    private AiSecretStoreService aiSecretStoreService;
    @Mock
    private EmailTemplateLandingPageBindingService bindingService;
    @Mock
    private PayloadTypeService payloadTypeService;

    @InjectMocks
    private EmailTemplateServiceImpl emailTemplateService;

    @BeforeEach
    void setUpContext() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(CLIENT_ID)
                .clientAdminId("client-admin-1")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(aiSecretStoreService.resolveCredentials(eq(AiProviderType.OPENAI), eq(CLIENT_ID), eq(null)))
                .thenReturn(AiResolvedCredentials.builder().apiKey("test-key").build());
        when(emailTemplateRepository.existsByClientIdAndTemplateName("client-admin-1", "AI Template"))
                .thenReturn(false);
        when(aiGenerationJobRepository.save(any(AiGenerationJob.class))).thenAnswer(inv -> {
            AiGenerationJob job = inv.getArgument(0);
            job.setId("job-1");
            return job;
        });
        when(emailTemplateMapper.toDto(any(EmailTemplate.class), eq(true), eq(true)))
                .thenReturn(EmailTemplateDto.builder().templateId(TEMPLATE_ID).build());
    }

    @Test
    void generateAITemplate_WithLandingPageIds_BindsAfterStubSave() {
        AITemplateGenerateRequest request = aiRequest(List.of(PAGE_ID));

        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenAnswer(inv -> {
            EmailTemplate entity = inv.getArgument(0);
            entity.setId(TEMPLATE_ID);
            return entity;
        });
        when(emailTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(
                EmailTemplate.builder().id(TEMPLATE_ID).landingPageIds(List.of(PAGE_ID)).build()));

        emailTemplateService.generateAITemplate(request);

        verify(bindingService).setLandingPagesForTemplate(TEMPLATE_ID, List.of(PAGE_ID), CLIENT_ID);
    }

    @Test
    void generateAITemplate_WithoutLandingPageIds_SkipsBinding() {
        AITemplateGenerateRequest request = aiRequest(null);

        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenAnswer(inv -> {
            EmailTemplate entity = inv.getArgument(0);
            entity.setId(TEMPLATE_ID);
            return entity;
        });

        emailTemplateService.generateAITemplate(request);

        verify(bindingService, never()).setLandingPagesForTemplate(any(), any(), any());
    }

    private AITemplateGenerateRequest aiRequest(List<String> landingPageIds) {
        return AITemplateGenerateRequest.builder()
                .templateName("AI Template")
                .payloadType(PayloadTypeDto.builder().id("payload-1").build())
                .emailSubject("Subject")
                .attackerPersona(AttackerPersonaDto.builder().id("persona-1").build())
                .attackTechnique(AttackTechniqueDto.builder().id("technique-1").build())
                .triggerEvent(TriggerEventDto.builder().id("trigger-1").build())
                .expectedUserAction(ExpectedUserActionDto.builder().id("action-1").build())
                .socialEngineeringStrategy(SocialEngineeringStrategyDto.builder().id("strategy-1").build())
                .campaignObjective(CampaignObjectiveDto.builder().id("objective-1").build())
                .difficultyLevel(DifficultyDto.builder().id("difficulty-1").build())
                .providerType(AiProviderType.OPENAI)
                .landingPageIds(landingPageIds)
                .build();
    }
}
