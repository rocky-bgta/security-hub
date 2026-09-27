package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.secret.AiSecretStoreService;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.request.AILandingPageRequest;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.mapper.LandingPageMapper;
import com.aspire.asat.phishing.model.AiGenerationJob;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.AiGenerationJobRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.AiContentGenerationSqsService;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import com.aspire.asat.phishing.service.support.CatalogReferenceResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AILandingPageServiceImplBindingTest {

    private static final String CLIENT_ID = "user-1";
    private static final String PAGE_ID = "page-ai-1";
    private static final String TEMPLATE_ID = "template-1";

    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private AiSecretStoreService aiSecretStoreService;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private LandingPageMapper landingPageMapper;
    @Mock
    private CatalogReferenceResolver catalogReferenceResolver;
    @Mock
    private AiGenerationJobRepository aiGenerationJobRepository;
    @Mock
    private AiContentGenerationSqsService aiContentGenerationSqsService;
    @Mock
    private EmailTemplateLandingPageBindingService bindingService;

    @InjectMocks
    private AILandingPageServiceImpl aiLandingPageService;

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
        when(aiGenerationJobRepository.save(any(AiGenerationJob.class))).thenAnswer(inv -> {
            AiGenerationJob job = inv.getArgument(0);
            job.setId("job-1");
            return job;
        });
        when(landingPageRepository.save(any(LandingPage.class))).thenAnswer(inv -> {
            LandingPage page = inv.getArgument(0);
            page.setId(PAGE_ID);
            return page;
        });
        when(landingPageMapper.toDto(any(LandingPage.class), eq(true), eq(true)))
                .thenReturn(LandingPageDto.builder().pageId(PAGE_ID).build());
    }

    @Test
    void generateLandingPage_WithEmailTemplateIds_BindsAndReturnsIds() {
        AILandingPageRequest request = aiRequest(List.of(TEMPLATE_ID));
        when(bindingService.getBoundEmailTemplateIds(PAGE_ID, CLIENT_ID))
                .thenReturn(List.of(TEMPLATE_ID));

        LandingPageDto result = aiLandingPageService.generateLandingPage(request);

        verify(bindingService).addLandingPageToTemplates(PAGE_ID, List.of(TEMPLATE_ID), CLIENT_ID);
        assertEquals(List.of(TEMPLATE_ID), result.getEmailTemplateIds());
    }

    @Test
    void generateLandingPage_WithoutEmailTemplateIds_SkipsBinding() {
        AILandingPageRequest request = aiRequest(null);
        when(bindingService.getBoundEmailTemplateIds(PAGE_ID, CLIENT_ID)).thenReturn(List.of());

        aiLandingPageService.generateLandingPage(request);

        verify(bindingService, never()).addLandingPageToTemplates(any(), any(), any());
    }

    private AILandingPageRequest aiRequest(List<String> emailTemplateIds) {
        return AILandingPageRequest.builder()
                .name("AI Landing Page")
                .pageType(LandingPageType.LANDING_PAGE)
                .providerType(AiProviderType.OPENAI)
                .emailTemplateIds(emailTemplateIds)
                .build();
    }
}
