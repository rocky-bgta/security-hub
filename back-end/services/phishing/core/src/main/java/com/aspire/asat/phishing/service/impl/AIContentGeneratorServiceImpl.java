package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.constant.AIGenerationConstants;
import com.aspire.asat.phishing.ai.adapter.AiProviderAdapter;
import com.aspire.asat.phishing.ai.adapter.AiProviderAdapterFactory;
import com.aspire.asat.phishing.ai.model.AiEmailRequest;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.AiGenerationInput;
import com.aspire.asat.phishing.ai.model.AiGenerationOptions;
import com.aspire.asat.phishing.ai.model.AiGeneratedEmailContent;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.secret.AiSecretStoreService;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.request.FixEmailTemplateRequest;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.AIContentGeneratorService;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.aspire.asat.phishing.utils.AiClientAdminIdSupport;
import com.aspire.asat.phishing.utils.CatalogDtoUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of AIContentGeneratorService.
 * Generates phishing email template content using AI.
 * Based on BRD Use Case 2.1.3.2
 * 
 * TODO: Integrate with actual AI service (OpenAI, Azure AI, etc.)
 * This implementation provides a placeholder that returns mock content.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIContentGeneratorServiceImpl implements AIContentGeneratorService {

    private final UserCurrentContextService userCurrentContextService;
    private final AiSecretStoreService aiSecretStoreService;
    private final AiProviderAdapterFactory aiProviderAdapterFactory;
    private final HtmlSanitizerService htmlSanitizerService;
    
    @Override
    public AIGeneratedContent generateTemplateContent(AITemplateGenerateRequest request) {
        if (request == null) {
            throw new ServiceException("AITemplateGenerateRequest is required");
        }
        String clientAdminId = AiClientAdminIdSupport.resolve(userCurrentContextService.getCurrentUserContext());
        return generateTemplateContent(request, clientAdminId);
    }

    @Override
    public AIGeneratedContent generateTemplateContent(AITemplateGenerateRequest request, String clientAdminId) {
        if (request == null) {
            throw new ServiceException("AITemplateGenerateRequest is required");
        }
        if (clientAdminId == null || clientAdminId.isBlank()) {
            throw new ServiceException("clientAdminId is required for AI operations");
        }
        try {
            AiProviderType providerType = request.getProviderType();

            AiResolvedCredentials resolvedCredentials = aiSecretStoreService.resolveCredentials(
                    providerType,
                    clientAdminId,
                    null
            );

            AiGenerationOptions options = mapOptions(request);

            AiEmailRequest aiRequest = AiEmailRequest.builder()
                    .providerType(providerType)
                    .model(request.getModel())
                    .apiKey(resolvedCredentials.getApiKey())
                    .apiSecret(resolvedCredentials.getApiSecret())
                    .templateName(request.getTemplateName())
                    .payloadType(CatalogDtoUtils.getName(request.getPayloadType()))
                    .options(options)
                    .build();

            AiProviderAdapter adapter = aiProviderAdapterFactory.getAdapter(providerType);
            AiGeneratedEmailContent generated = adapter.generateEmailTemplate(aiRequest);

            if (!generated.isSuccess()) {
                return new AIGeneratedContent(
                        null,
                        null,
                        null,
                        false,
                        generated.getErrorMessage()
                );
            }

            if (generated.getHtmlBody() == null || generated.getHtmlBody().isBlank()) {
                return new AIGeneratedContent(
                        null,
                        null,
                        null,
                        false,
                        "AI generation produced empty htmlBody"
                );
            }

            return new AIGeneratedContent(
                    generated.getHtmlBody(),
                    generated.getTextBody(),
                    generated.getSuggestedDifficulty(),
                    true,
                    null
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI email template generation failed: {}", e.getMessage(), e);
            throw new ServiceException("AI content generation failed: " + e.getMessage());
        }
    }

    @Override
    public String fixEmailTemplate(FixEmailTemplateRequest request) {
        if (request == null) {
            throw new ServiceException("FixEmailTemplateRequest is required");
        }

        String clientAdminId = AiClientAdminIdSupport.resolve(userCurrentContextService.getCurrentUserContext());
        AiProviderType providerType = request.getProviderType();

        AiResolvedCredentials resolvedCredentials = aiSecretStoreService.resolveCredentials(
                providerType,
                clientAdminId,
                null
        );

        AiFixHtmlPageRequest aiRequest = AiFixHtmlPageRequest.builder()
                .providerType(providerType)
                .model(request.getModel())
                .input(AiGenerationInput.builder()
                        .prompt(request.getPrompt())
                        .elementHtml(request.getElementHtml())
                        .templateCode(request.getTemplateCode())
                        .build())
                .build();

        AiProviderAdapter adapter = aiProviderAdapterFactory.getAdapter(providerType);
        String fixedHtml = adapter.fixEmailTemplate(aiRequest, resolvedCredentials);

        if (fixedHtml == null || fixedHtml.isBlank()) {
            throw new ServiceException("AI generation produced empty html content");
        }

        return htmlSanitizerService.sanitize(fixedHtml);
    }
    
    @Override
    public List<String> getTargetIndustries() {
        return AIGenerationConstants.TARGET_INDUSTRIES;
    }
    
    @Override
    public List<String> getAttackerPersonas() {
        return AIGenerationConstants.ATTACKER_PERSONAS;
    }
    
    @Override
    public List<String> getSocialEngineeringStrategies() {
        return AIGenerationConstants.SOCIAL_ENGINEERING_STRATEGIES;
    }
    
    @Override
    public List<String> getCampaignObjectives() {
        return AIGenerationConstants.CAMPAIGN_OBJECTIVES;
    }
    
    private AiGenerationOptions mapOptions(AITemplateGenerateRequest request) {
        var genOptions = request.getGenerationOptions();
        String difficulty = CatalogDtoUtils.getName(request.getDifficultyLevel());
        String targetIndustry = CatalogDtoUtils.getName(request.getTargetIndustry());
        String department = CatalogDtoUtils.getName(request.getDepartment());

        // Keep prompt context mutually exclusive as requested:
        // department takes precedence when both are accidentally provided.
        if (department != null && !department.isBlank()) {
            targetIndustry = null;
        }

        List<String> employeeData = request.getEmployeeDataRequired() == null
                ? null
                : new ArrayList<>(request.getEmployeeDataRequired());

        return AiGenerationOptions.builder()
                .tone(genOptions != null ? CatalogDtoUtils.getName(genOptions.getTone()) : null)
                .language(resolveOutputLanguage(genOptions != null ? genOptions.getLanguage() : null))
                .inputLanguage(request.getInputLanguage())
                .difficulty(difficulty)
                .difficultyLevel(difficulty)
                .brand(genOptions != null ? CatalogDtoUtils.getName(genOptions.getBrand()) : null)
                .contentLength(genOptions != null ? genOptions.getContentLength() : null)
                .callToAction(genOptions != null ? CatalogDtoUtils.getName(genOptions.getCallToAction()) : null)
                .urgencyLevel(genOptions != null ? CatalogDtoUtils.getName(genOptions.getUrgencyLevel()) : null)
                .emotionalTrigger(genOptions != null ? CatalogDtoUtils.getName(genOptions.getEmotionalTrigger()) : null)
                .constraints(genOptions != null ? CatalogDtoUtils.getName(genOptions.getConstraints()) : null)
                .targetIndustry(targetIndustry)
                .department(department)
                .attackerPersona(CatalogDtoUtils.getName(request.getAttackerPersona()))
                .attackTechnique(CatalogDtoUtils.getName(request.getAttackTechnique()))
                .triggerEvent(CatalogDtoUtils.getName(request.getTriggerEvent()))
                .expectedUserAction(CatalogDtoUtils.getName(request.getExpectedUserAction()))
                .socialEngineeringStrategy(CatalogDtoUtils.getName(request.getSocialEngineeringStrategy()))
                .campaignObjective(CatalogDtoUtils.getName(request.getCampaignObjective()))
                .generationMode(request.getGenerationMode())
                .emailSubject(request.getEmailSubject())
                .description(request.getDescription())
                .additionalContext(request.getAdditionalContext())
                .voiceInputContent(request.getVoiceInputContent())
                .tags(request.getTags())
                .employeeDataRequired(employeeData)
                .build();
    }

    private String resolveOutputLanguage(String requestedLanguage) {
        return StringUtils.hasText(requestedLanguage) ? requestedLanguage.trim() : "English";
    }

}
