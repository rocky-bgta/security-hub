package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.ai.adapter.AiProviderAdapter;
import com.aspire.asat.phishing.ai.adapter.AiProviderAdapterFactory;
import com.aspire.asat.phishing.ai.model.AiGenerationOptions;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.AiGenerationInput;
import com.aspire.asat.phishing.ai.model.AiGeneratedLandingContent;
import com.aspire.asat.phishing.ai.model.AiLandingPageRequest;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.secret.AiSecretStoreService;
import com.aspire.asat.phishing.dto.enums.AiGenerationJobStatus;
import com.aspire.asat.phishing.dto.enums.AiGenerationJobType;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import com.aspire.asat.phishing.dto.request.AILandingPageRequest;
import com.aspire.asat.phishing.dto.request.FixHtmlPageRequest;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.dto.sqs.AiContentGenerationMessage;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.LandingPageMapper;
import com.aspire.asat.phishing.model.AiGenerationJob;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.AiGenerationJobRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.AILandingPageService;
import com.aspire.asat.phishing.service.AiContentGenerationSqsService;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.aspire.asat.phishing.service.support.CatalogReferenceResolver;
import com.aspire.asat.phishing.dto.request.AiGenerationOptionsRequest;
import com.aspire.asat.phishing.utils.AiClientAdminIdSupport;
import com.aspire.asat.phishing.utils.CatalogDtoUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AILandingPageService.
 * Generates landing pages using AI based on various modes.
 * Based on Task-05 Landing Page Creation (AC-10, AC-11)
 * 
 * TODO: Integrate with actual AI service (OpenAI, Azure AI, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AILandingPageServiceImpl implements AILandingPageService {
    
    private final HtmlSanitizerService htmlSanitizerService;
    private final UserCurrentContextService userCurrentContextService;
    private final AiSecretStoreService aiSecretStoreService;
    private final AiProviderAdapterFactory aiProviderAdapterFactory;
    private final LandingPageRepository landingPageRepository;
    private final LandingPageMapper landingPageMapper;
    private final CatalogReferenceResolver catalogReferenceResolver;
    private final AiGenerationJobRepository aiGenerationJobRepository;
    private final AiContentGenerationSqsService aiContentGenerationSqsService;
    private final EmailTemplateLandingPageBindingService bindingService;
    
    // Generation modes
    public static final String MODE_CLONE_STYLE = "CLONE_STYLE";
    public static final String MODE_BRAND_BASED = "BRAND_BASED";
    public static final String MODE_FULLY_AI = "FULLY_AI";
    
    // Layout styles
    public static final String LAYOUT_MINIMAL = "MINIMAL";
    public static final String LAYOUT_CORPORATE = "CORPORATE";
    public static final String LAYOUT_MOBILE_FIRST = "MOBILE_FIRST";
    public static final String LAYOUT_DARK_MODE = "DARK_MODE";
    
    @Override
    public LandingPageDto generateLandingPage(AILandingPageRequest request) {
        if (request == null) {
            throw new ServiceException("AILandingPageRequest is required");
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String clientId = AiClientAdminIdSupport.resolve(userContext);
        String userId = userContext != null ? userContext.getUserId() : null;
        UserType userType = parseUserType(userContext);

        if (userType != UserType.USER) {
            clientId = userId;
        }
        AiProviderType providerType = request.getProviderType();
        String clientAdminId = clientId;
        AiResolvedCredentials resolvedCredentials = aiSecretStoreService.resolveCredentials(
                providerType,
                clientAdminId,
                null
        );
        if (resolvedCredentials == null || !StringUtils.hasText(resolvedCredentials.getApiKey())) {
            throw new ServiceException("AI configuration is done for the provider: " + providerType);
        }

        // Persist a stub LandingPage with status=PROCESSING; the AI vendor call runs
        // asynchronously in the SQS listener and fills in htmlContent on success.
        LandingPage stub = LandingPage.builder()
                .clientId(clientId)
                .name(request.getName())
                .description(request.getDescription())
                .pageType(request.getPageType())
                .category(catalogReferenceResolver.resolveCategory(request.getCategory()))
                .difficultyLevel(catalogReferenceResolver.resolveDifficulty(request.getDifficultyLevel()))
                .department(request.getDepartment())
                .targetDepartment(request.getTargetDepartment())
                .targetIndustry(request.getTargetIndustry())
                .constraints(generationOptionsConstraints(request))
                .urgencyLevel(resolveUrgencyLevel(request))
                .emotionalTrigger(resolveEmotionalTrigger(request))
                .dataCaptureType(catalogReferenceResolver.resolveDataCaptureType(request.getDataCaptureType()))
                .status(LandingPageStatus.PROCESSING)
                .htmlContent("")
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .captureSubmittedData(false)
                .captureFields(new ArrayList<>())
                .popularity(0)
                .isGlobal(isGlobalPage(userType))
                .isPremium(false)
                .templateGenerationType(request.getTemplateGenerationType() != null
                        ? request.getTemplateGenerationType()
                        : TemplateGenerationType.AI)
                .createdBy(userId)
                .createdByRole(userType != null ? userType.getValue() : null)
                .build();

        LandingPage savedStub = landingPageRepository.save(stub);

        if (request.getEmailTemplateIds() != null && !request.getEmailTemplateIds().isEmpty()) {
            bindingService.addLandingPageToTemplates(savedStub.getId(), request.getEmailTemplateIds(), clientId);
        }

        AiGenerationJob job = AiGenerationJob.builder()
                .jobType(AiGenerationJobType.LANDING_PAGE)
                .status(AiGenerationJobStatus.PROCESSING)
                .clientId(clientId)
                .createdBy(userId)
                .createdByRole(userType != null ? userType.getValue() : null)
                .targetEntityId(savedStub.getId())
                .landingPageRequest(request)
                .attempts(0)
                .build();
        AiGenerationJob savedJob = aiGenerationJobRepository.save(job);

        savedStub.setAiGenerationJobId(savedJob.getId());
        landingPageRepository.save(savedStub);

        try {
            aiContentGenerationSqsService.sendMessage(AiContentGenerationMessage.builder()
                    .jobId(savedJob.getId())
                    .jobType(AiGenerationJobType.LANDING_PAGE)
                    .build());
        } catch (RuntimeException e) {
            // Roll back to FAILED so the user does not see a permanent PROCESSING page
            // when we cannot enqueue the job (e.g. SQS unavailable).
            String reason = e.getMessage() != null ? e.getMessage() : "Failed to enqueue AI generation job";
            savedStub.setStatus(LandingPageStatus.FAILED);
            savedStub.setAiErrorMessage(reason);
            landingPageRepository.save(savedStub);

            savedJob.setStatus(AiGenerationJobStatus.FAILED);
            savedJob.setErrorMessage(reason);
            aiGenerationJobRepository.save(savedJob);

            log.error("Failed to enqueue AI landing page generation jobId={} pageId={}",
                    savedJob.getId(), savedStub.getId(), e);
            throw e;
        }

        log.info("AI landing page {} accepted (jobId={}) for user {} with userType {}",
                savedStub.getId(), savedJob.getId(), userId,
                userType != null ? userType.getValue() : null);

        LandingPageDto dto = landingPageMapper.toDto(savedStub, true, true);
        dto.setEmailTemplateIds(bindingService.getBoundEmailTemplateIds(savedStub.getId(), clientId));
        return dto;
    }

    @Override
    public String generateLandingPageHtml(AILandingPageRequest request, String clientAdminId) {
        if (request == null) {
            throw new ServiceException("AILandingPageRequest is required");
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

            AiLandingPageRequest aiRequest = AiLandingPageRequest.builder()
                    .providerType(providerType)
                    .model(request.getModel())
                    .apiKey(resolvedCredentials.getApiKey())
                    .apiSecret(resolvedCredentials.getApiSecret())
                    .pageName(request.getName())
                    .pageType(request.getPageType() != null ? request.getPageType().name() : null)
                    .category(categoryNameForPrompt(request.getCategory()))
                    .targetDepartment(CatalogDtoUtils.getName(request.getTargetDepartment()))
                    .urgencyLevel(CatalogDtoUtils.getName(resolveUrgencyLevel(request)))
                    .emotionalTrigger(CatalogDtoUtils.getName(resolveEmotionalTrigger(request)))
                    .voiceInput(request.getVoiceInput())
                    .additionalContext(request.getAdditionalContext())
                    .options(options)
                    .build();

            AiProviderAdapter adapter = aiProviderAdapterFactory.getAdapter(providerType);
            AiGeneratedLandingContent generated = adapter.generateLandingPage(aiRequest);

            if (!generated.isSuccess()) {
                throw new ServiceException("AI generation failed: " + generated.getErrorMessage());
            }

            if (generated.getHtmlContent() == null || generated.getHtmlContent().isBlank()) {
                throw new ServiceException("AI generation produced empty htmlContent");
            }

            return htmlSanitizerService.sanitize(generated.getHtmlContent());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI landing page generation failed: {}", e.getMessage(), e);
            throw new ServiceException("AI generation failed: " + e.getMessage());
        }
    }

    private UserType parseUserType(CurrentUserContext context) {
        if (context == null || context.getUserType() == null) {
            return null;
        }
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private boolean isGlobalPage(UserType userType) {
        return userType == UserType.SUPER_ADMIN
                || userType == UserType.ASPIRE_ADMIN
                || userType == UserType.SYSTEM_USER;
    }

    /**
     * Builds {@link AiGenerationOptions} from top-level {@link AILandingPageRequest} and nested
     * {@link AiGenerationOptionsRequest}. Nested options only supply tone, brand, contentLength, callToAction, and
     * language; brand and language fall back to {@code brandName} / {@code inputLanguage} on the parent when blank.
     */
    private AiGenerationOptions mapOptions(AILandingPageRequest request) {
        AiGenerationOptionsRequest g = request.getGenerationOptions();

        return AiGenerationOptions.builder()
                .tone(g != null ? CatalogDtoUtils.getName(g.getTone()) : null)
                .brand(g != null ? CatalogDtoUtils.getName(g.getBrand()) : null)
                .language(resolveOutputLanguage(coalesce(g != null ? g.getLanguage() : null, request.getInputLanguage())))
                .inputLanguage(request.getInputLanguage())
                .difficultyLevel(difficultyNameForPrompt(request.getDifficultyLevel()))
                .department(CatalogDtoUtils.getName(request.getDepartment()))
                .targetIndustry(CatalogDtoUtils.getName(request.getTargetIndustry()))
                .targetDepartment(CatalogDtoUtils.getName(request.getTargetDepartment()))
                .urgencyLevel(CatalogDtoUtils.getName(resolveUrgencyLevel(request)))
                .emotionalTrigger(CatalogDtoUtils.getName(resolveEmotionalTrigger(request)))
                .dataCaptureType(dataCaptureTypeNameForPrompt(request.getDataCaptureType()))
                .constraints(CatalogDtoUtils.getName(generationOptionsConstraints(request)))
                .additionalContext(request.getAdditionalContext())
                .contentLength(g != null ? g.getContentLength() : null)
                .callToAction(g != null ? CatalogDtoUtils.getName(g.getCallToAction()) : null)
                .generationMode(request.getGenerationMode())
                .layoutStyle(request.getLayoutStyle())
                .description(request.getDescription())
                .voiceInput(request.getVoiceInput())
                .tags(request.getTags())
                .build();
    }

    private static String coalesce(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred.trim() : (fallback != null ? fallback.trim() : null);
    }

    private static String resolveOutputLanguage(String requestedLanguage) {
        return StringUtils.hasText(requestedLanguage) ? requestedLanguage.trim() : "English";
    }

    private String categoryNameForPrompt(LandingPageCategoryDto category) {
        LandingPageCategoryDto resolved = catalogReferenceResolver.resolveCategory(category);
        return resolved != null ? resolved.getName() : null;
    }

    private String difficultyNameForPrompt(DifficultyDto difficulty) {
        DifficultyDto resolved = catalogReferenceResolver.resolveDifficulty(difficulty);
        return resolved != null ? resolved.getName() : null;
    }

    private String dataCaptureTypeNameForPrompt(DataCaptureTypeDto dataCaptureType) {
        DataCaptureTypeDto resolved = catalogReferenceResolver.resolveDataCaptureType(dataCaptureType);
        return resolved != null ? resolved.getName() : null;
    }

    private ConstraintsDataDto generationOptionsConstraints(AILandingPageRequest request) {
        if (request == null || request.getGenerationOptions() == null) {
            return null;
        }
        return request.getGenerationOptions().getConstraints();
    }

    private UrgencyLevelDto resolveUrgencyLevel(AILandingPageRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getUrgencyLevel() != null) {
            return request.getUrgencyLevel();
        }
        return request.getGenerationOptions() != null ? request.getGenerationOptions().getUrgencyLevel() : null;
    }

    private EmotionalTriggerDto resolveEmotionalTrigger(AILandingPageRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getEmotionalTrigger() != null) {
            return request.getEmotionalTrigger();
        }
        return request.getGenerationOptions() != null ? request.getGenerationOptions().getEmotionalTrigger() : null;
    }

    @Override
    public LandingPageDto regenerateLandingPage(AILandingPageRequest request) {
        // BR-04: Admin can regenerate AI output unlimited times
        log.info("Regenerating landing page with AI");
        if (request != null && request.getName() != null) {
            // Landing pages are unique by name per client; regeneration should not fail due to duplicates.
            request.setName(request.getName() + " (AI Regenerated)");
        }
        return generateLandingPage(request);
    }
    
    @Override
    public String generateHtmlContent(String mode, Map<String, String> params) {
        // TODO: Integrate with actual AI service
        // This is a placeholder implementation that generates template-based HTML
        
        String layoutStyle = params.getOrDefault("layoutStyle", LAYOUT_CORPORATE);
        String brandName = params.getOrDefault("brandName", "Example Company");
        String category = params.getOrDefault("category", "BUSINESS");
        
        switch (mode) {
            case MODE_CLONE_STYLE:
                return generateCloneStyleHtml(params);
            case MODE_BRAND_BASED:
                return generateBrandBasedHtml(brandName, layoutStyle, category);
            case MODE_FULLY_AI:
            default:
                return generateFullyAiHtml(layoutStyle, category, params);
        }
    }

    @Override
    public String fixHtmlPage(FixHtmlPageRequest request) {
        if (request == null) {
            throw new ServiceException("FixHtmlPageRequest is required");
        }
        if (request.getInput() == null) {
            throw new ServiceException("input is required");
        }
        try {
            String clientAdminId = AiClientAdminIdSupport.resolve(userCurrentContextService.getCurrentUserContext());
            AiResolvedCredentials resolvedCredentials = aiSecretStoreService.resolveCredentials(
                    request.getProviderType(),
                    clientAdminId,
                    null
            );

            AiFixHtmlPageRequest aiRequest = AiFixHtmlPageRequest.builder()
                    .providerType(request.getProviderType())
                    .model(request.getModel())
                    .input(AiGenerationInput.builder()
                            .prompt(request.getInput().getPrompt())
                            .elementHtml(request.getInput().getElementHtml())
                            .templateCode(request.getInput().getTemplateCode())
                            .build())
                    .build();

            AiProviderAdapter adapter = aiProviderAdapterFactory.getAdapter(request.getProviderType());
            String fixedHtml = adapter.fixHtmlPage(aiRequest, resolvedCredentials);

            if (fixedHtml == null || fixedHtml.isBlank()) {
                throw new ServiceException("AI generation produced empty html content");
            }

            return htmlSanitizerService.sanitize(fixedHtml);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI html fix failed for provider {}: {}", request.getProviderType(), e.getMessage(), e);
            throw new ServiceException("AI fix failed: " + e.getMessage());
        }
    }
    
    /**
     * Build generation parameters from request
     */
    private Map<String, String> buildGenerationParams(AILandingPageRequest request) {
        Map<String, String> params = new HashMap<>();
        
        params.put("name", request.getName());
        params.put("description", request.getDescription() != null ? request.getDescription() : "");
        params.put("pageType", request.getPageType().name());
        params.put("layoutStyle", request.getLayoutStyle() != null ? request.getLayoutStyle() : LAYOUT_CORPORATE);

        if (request.getVoiceInput() != null) {
            params.put("voiceInput", request.getVoiceInput());
        }
        
        return params;
    }
    
    /**
     * Generate clone-style HTML (based on target URL styling)
     */
    private String generateCloneStyleHtml(Map<String, String> params) {
        String targetUrl = params.getOrDefault("targetUrl", "");
        String layoutStyle = params.getOrDefault("layoutStyle", LAYOUT_CORPORATE);
        
        // Placeholder - actual implementation would analyze target URL styling
        return generateFullyAiHtml(layoutStyle, params.getOrDefault("category", "BUSINESS"), params);
    }
    
    /**
     * Generate brand-based HTML
     */
    private String generateBrandBasedHtml(String brandName, String layoutStyle, String category) {
        String cssClass = getCssClassForLayout(layoutStyle);
        String primaryColor = getPrimaryColorForCategory(category);
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - Sign In</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif; }
                    body { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: %s; }
                    .container { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); width: 100%%; max-width: 400px; }
                    .logo { text-align: center; margin-bottom: 30px; font-size: 24px; font-weight: bold; color: %s; }
                    .form-group { margin-bottom: 20px; }
                    label { display: block; margin-bottom: 8px; font-weight: 500; color: #333; }
                    input { width: 100%%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 16px; }
                    input:focus { outline: none; border-color: %s; }
                    button { width: 100%%; padding: 14px; background: %s; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
                    button:hover { opacity: 0.9; }
                    .footer { text-align: center; margin-top: 20px; font-size: 14px; color: #666; }
                </style>
            </head>
            <body class="%s">
                <div class="container">
                    <div class="logo">%s</div>
                    <form action="#" method="POST">
                        <div class="form-group">
                            <label for="email">Email Address</label>
                            <input type="email" id="email" name="email" placeholder="Enter your email" required>
                        </div>
                        <div class="form-group">
                            <label for="password">Password</label>
                            <input type="password" id="password" name="password" placeholder="Enter your password" required>
                        </div>
                        <button type="submit">Sign In</button>
                    </form>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, brandName, getBackgroundColor(layoutStyle), primaryColor, primaryColor, 
            primaryColor, cssClass, brandName, brandName);
    }
    
    /**
     * Generate fully AI-powered HTML
     */
    private String generateFullyAiHtml(String layoutStyle, String category, Map<String, String> params) {
        String name = params.getOrDefault("name", "Landing Page");
        String description = params.getOrDefault("description", "Welcome to our secure portal");
        
        return generateBrandBasedHtml(name, layoutStyle, category);
    }
    
    /**
     * Get CSS class for layout style
     */
    private String getCssClassForLayout(String layoutStyle) {
        return switch (layoutStyle) {
            case LAYOUT_MINIMAL -> "minimal-layout";
            case LAYOUT_MOBILE_FIRST -> "mobile-first-layout";
            case LAYOUT_DARK_MODE -> "dark-mode-layout";
            default -> "corporate-layout";
        };
    }
    
    /**
     * Get primary color for category
     */
    private String getPrimaryColorForCategory(String category) {
        return switch (category) {
            case "SOCIAL_MEDIA" -> "#1da1f2";
            case "EMAIL_PROVIDER" -> "#ea4335";
            case "CLOUD_APP" -> "#0078d4";
            case "FINANCIAL" -> "#00a550";
            case "GOVERNMENT" -> "#003366";
            case "HEALTHCARE" -> "#e91e63";
            default -> "#2563eb";
        };
    }
    
    /**
     * Get background color for layout
     */
    private String getBackgroundColor(String layoutStyle) {
        return switch (layoutStyle) {
            case LAYOUT_DARK_MODE -> "#1a1a2e";
            case LAYOUT_MINIMAL -> "#ffffff";
            default -> "#f5f5f5";
        };
    }
}
