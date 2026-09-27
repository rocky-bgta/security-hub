package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.secret.AiSecretStoreService;
import com.aspire.asat.phishing.dto.enums.AiGenerationJobStatus;
import com.aspire.asat.phishing.dto.enums.AiGenerationJobType;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.EmailType;
import com.aspire.asat.phishing.dto.enums.EmployeeDataField;
import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.request.EmailTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.EmailTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.EmailTemplatePreviewDto;
import com.aspire.asat.phishing.dto.response.FilterOptionsDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.SanitizeHtmlResult;
import com.aspire.asat.phishing.dto.sqs.AiContentGenerationMessage;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.EmailTemplateMapper;
import com.aspire.asat.phishing.model.AiGenerationJob;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.repository.AiGenerationJobRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.service.AiContentGenerationSqsService;
import com.aspire.asat.phishing.service.DifficultyService;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import com.aspire.asat.phishing.service.EmailTemplateService;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.aspire.asat.phishing.service.PayloadTypeService;
import com.aspire.asat.phishing.service.support.PayloadTypeChannelSupport;
import com.aspire.asat.phishing.service.support.SmsTemplateValidator;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of EmailTemplateService.
 * Handles email template CRUD operations with permission checking.
 * Based on BRD Use Case 2.1.3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {
    
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateMapper emailTemplateMapper;
    private final UserCurrentContextService userCurrentContextService;
    private final HtmlSanitizerService htmlSanitizerService;
    private final AiGenerationJobRepository aiGenerationJobRepository;
    private final AiContentGenerationSqsService aiContentGenerationSqsService;
    private final AiSecretStoreService aiSecretStoreService;
    private final PayloadTypeService payloadTypeService;
    private final DifficultyService difficultyService;
    private final SmsTemplateValidator smsTemplateValidator;
    private final EmailTemplateLandingPageBindingService bindingService;
    
    private static final String ROLE_SUPER_ADMIN = UserType.SUPER_ADMIN.getValue();
    private static final String ROLE_ASPIRE_ADMIN = UserType.ASPIRE_ADMIN.getValue();
    private static final String ROLE_CLIENT_ADMIN = UserType.CLIENT_ADMIN.getValue();

    private static final int MAX_ATTACHMENTS = 5;
    
    @Override
    public List<EmailTemplateDto> getTemplates(
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            String clientId,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        
        String userId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        boolean isAspireAdmin = currentUserType == UserType.ASPIRE_ADMIN || currentUserType == UserType.SYSTEM_USER || currentUserType == UserType.SUPER_ADMIN;
        if(!isAspireAdmin) {
            clientId = userId;
        }

        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(pageNumber, effectivePageSize, sort);

        List<EmailTemplate> templates = emailTemplateRepository.findWithFilters(
                clientId,
                searchParam,
                difficultyLevelId,
                payloadTypeId,
                location,
                tags,
                language,
                status,
                templateType,
                isAspireAdmin,
                pageable);

        return templates.stream()
                .map(template -> {
                    boolean canEdit = canEditTemplate(template, currentUserType);
                    boolean canDelete = canDeleteTemplate(template, currentUserType);
                    return emailTemplateMapper.toListItemDto(template, canEdit, canDelete);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public long countTemplates(
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            String clientId) {
        String userId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        boolean isAspireAdmin = currentUserType == UserType.ASPIRE_ADMIN || currentUserType == UserType.SYSTEM_USER || currentUserType == UserType.SUPER_ADMIN;

        if(!isAspireAdmin) {
            clientId = userId;
        }

        return emailTemplateRepository.countWithFilters(
                clientId,
                searchParam,
                difficultyLevelId,
                payloadTypeId,
                location,
                tags,
                language,
                status,
                templateType,
                isAspireAdmin);
    }
    
    @Override
    public Optional<EmailTemplateDto> getTemplateById(String templateId) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        return emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .map(template -> {
                    boolean canEdit = canEditTemplate(template, currentUserType);
                    boolean canDelete = canDeleteTemplate(template, currentUserType);
                    return emailTemplateMapper.toDto(template, canEdit, canDelete);
                });
    }
    
    @Override
    public Optional<EmailTemplatePreviewDto> getTemplatePreview(String templateId) {
        String clientId = getCurrentClientId();
        
        return emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .map(emailTemplateMapper::toPreviewDto);
    }
    
    @Override
    @Transactional
    public EmailTemplateDto updateTemplate(String templateId, EmailTemplateUpdateRequest request) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        EmailTemplate template = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        
        // Permission rules:
        // - CLIENT_ADMIN can update only templates they created (non-global).
        // - ASPIRE_ADMIN / SUPER_ADMIN templates are visible to client admins (isGlobal=true) but read-only for them.
        if (!canEditTemplate(template, currentUserType)) {
            throw new ServiceException("You do not have permission to edit this template");
        }
        
        emailTemplateMapper.updateFromRequest(template, request);
        if (request.getAttachments() != null) {
            template.setAttachments(normalizeAttachmentUrls(request.getAttachments()));
        }

        if (request.getPayloadType() != null) {
            payloadTypeService.validatePayloadTypeForTemplate(template.getTemplateType(), request.getPayloadType());
        }

        if (template.getTemplateType() == TemplateType.SMS) {
            smsTemplateValidator.validateSmsBody(template.getSmsBody());
        } else {
            SanitizeHtmlResult sanitizeResult = htmlSanitizerService.sanitizeWithDetails(template.getEmailBody());
            template.setEmailBody(sanitizeResult.getSanitizedHtml());
        }

        EmailTemplate saved = emailTemplateRepository.save(template);

        if (request.getLandingPageIds() != null) {
            bindingService.setLandingPagesForTemplate(saved.getId(), request.getLandingPageIds(), clientId);
            saved = emailTemplateRepository.findById(saved.getId()).orElse(saved);
        }
        
        log.info("Template {} updated by user {}", templateId, getCurrentUserId());
        
        return emailTemplateMapper.toDto(saved, true, true);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(String templateId) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        EmailTemplate template = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        
        if (!canDeleteTemplate(template, currentUserType)) {
            throw new ServiceException("You do not have permission to delete this template");
        }
        
        // TODO: Check if template is used in active campaigns
        // if (emailTemplateRepository.isTemplateUsedInCampaign(templateId)) {
        //     throw new ServiceException("Cannot delete template. It is currently used by active campaigns");
        // }
        
        emailTemplateRepository.delete(template);
        
        log.info("Template {} deleted by user {}", templateId, getCurrentUserId());
    }
    
    @Override
    @Transactional
    public EmailTemplateDto duplicateTemplate(String templateId) {
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        UserType userType = getCurrentUserType();
        
        EmailTemplate original = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        
        // Create duplicate with "(Copy)" suffix (BR-11)
        EmailTemplate duplicate = emailTemplateMapper.createDuplicate(original, clientId, userId,
                userType != null ? userType.getValue() : null);
        
        EmailTemplate saved = emailTemplateRepository.save(duplicate);
        
        log.info("Template {} duplicated as {} by user {}", templateId, saved.getId(), userId);
        
        return emailTemplateMapper.toDto(saved, true, true);
    }
    
    @Override
    public List<String> getAvailableTags() {
        // Return predefined tags from BRD (BR-08)
        return EmployeeDataField.PREDEFINED_TAGS;
    }
    
    @Override
    public FilterOptionsDto getFilterOptions(PayloadTypeChannel payloadTypeChannel) {
        String clientId = getCurrentClientId();
        PayloadTypeChannel channel = PayloadTypeChannelSupport.effectiveChannel(payloadTypeChannel);
        
        // Get distinct values from database
        List<String> locations = emailTemplateRepository.findDistinctLocationsByClientId(clientId)
                .stream()
                .map(EmailTemplate::getServiceLocation)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> languages = emailTemplateRepository.findDistinctLanguagesByClientId(clientId)
                .stream()
                .map(EmailTemplate::getLanguage)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> difficultyIds = difficultyService.getDifficulties(null, true, 0, 500, "displayOrder", "asc")
                .stream()
                .map(DifficultyDto::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        List<String> payloadTypeIds = payloadTypeService.getPayloadTypes(
                        null, true, 0, 500, "displayOrder", "asc", channel)
                .stream()
                .map(PayloadTypeDto::getId)
                .filter(StringUtils::hasText)
                .toList();

        return FilterOptionsDto.builder()
                .difficultyLevels(difficultyIds)
                .payloadTypes(payloadTypeIds)
                .locations(locations)
                .tags(EmployeeDataField.PREDEFINED_TAGS)
                .languages(languages)
                .build();
    }
    
    @Override
    @Transactional
    public void incrementPopularity(String templateId) {
        emailTemplateRepository.findById(templateId).ifPresent(template -> {
            template.setPopularity(template.getPopularity() + 1);
            emailTemplateRepository.save(template);
        });
    }
    
    // --- Task-03: Template Creation Methods ---
    
    @Override
    @Transactional
    public EmailTemplateDto createTemplate(EmailTemplateCreateRequest request) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String clientId = context.getClientAdminId();
        String userId = context.getUserId();
        UserType userType = parseUserType(context);
        if (userType != UserType.USER) {
            clientId = userId;
        }

        // BR-09: Check template name uniqueness
        if (!isTemplateNameUnique(request.getTemplateName())) {
            throw new ServiceException("A template with this name already exists");
        }

        TemplateType templateType = request.getTemplateType() != null ? request.getTemplateType() : TemplateType.EMAIL;
        String sanitizedHtml = "";
        if (templateType == TemplateType.SMS) {
            smsTemplateValidator.validateSmsBody(request.getSmsBody());
        } else {
            if (!StringUtils.hasText(request.getEmailSubject()) || !StringUtils.hasText(request.getEmailBody())) {
                throw new ServiceException("Email subject and body are required for email templates");
            }
            SanitizeHtmlResult sanitizeResult = htmlSanitizerService.sanitizeWithDetails(request.getEmailBody());
            if (!sanitizeResult.isSafe()) {
                throw new ServiceException("HTML content contains potentially unsafe elements");
            }
            sanitizedHtml = sanitizeResult.getSanitizedHtml();
        }

        payloadTypeService.validatePayloadTypeForTemplate(templateType, request.getPayloadType());
        
        // Create new template entity
        EmailTemplate template = EmailTemplate.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .templateType(templateType)
                .emailType(request.getEmailType() != null ? request.getEmailType() : EmailType.STANDARD_PHISH)
                .payloadType(request.getPayloadType())
                .emailSubject(request.getEmailSubject())
                .emailBody(sanitizedHtml)
                .emailBodyText(request.getEmailBodyText())
                .smsBody(request.getSmsBody())
                .difficultyLevel(request.getDifficultyLevel())
                .department(request.getDepartment())
                .targetIndustry(request.getTargetIndustry())
                .constraints(request.getConstraints())
                .templateGenerationType(TemplateGenerationType.MANUAL)
                .serviceLocation(request.getServiceLocation())
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .employeeDataRequired(request.getEmployeeDataRequired() != null 
                        ? new ArrayList<>(request.getEmployeeDataRequired()) : new ArrayList<>())
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .clientId(clientId)
                .createdBy(userId)
                .lastModifiedBy(userId)
                .createdByRole(userType != null ? userType.getValue() : null)
                .popularity(0)
                .status(request.getStatus() != null ? request.getStatus() : EmailTemplateStatus.ACTIVE)
                .isGlobal(isGlobalTemplate(userType))
                .isPremium(false)
                .attachments(normalizeAttachmentUrls(request.getAttachments()))
                .build();
        
        EmailTemplate saved = emailTemplateRepository.save(template);

        if (request.getLandingPageIds() != null) {
            bindingService.setLandingPagesForTemplate(saved.getId(), request.getLandingPageIds(), clientId);
            saved = emailTemplateRepository.findById(saved.getId()).orElse(saved);
        }
        
        log.info("Template {} created by user {} with userType {}", saved.getId(), userId,
                userType != null ? userType.getValue() : null);
        
        return emailTemplateMapper.toDto(saved, true, true);
    }
    
    @Override
    @Transactional
    public EmailTemplateDto generateAITemplate(AITemplateGenerateRequest request) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String clientId = context.getClientAdminId();
        String userId = context.getUserId();
        UserType userType = parseUserType(context);

        if(userType != UserType.USER) {
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

        // BR-09: Check template name uniqueness
        if (!isTemplateNameUnique(request.getTemplateName())) {
            throw new ServiceException("A template with this name already exists");
        }

        payloadTypeService.validatePayloadTypeForTemplate(TemplateType.EMAIL, request.getPayloadType());

        // Persist a stub email template with status=PROCESSING; the AI vendor call runs
        // asynchronously in the SQS listener and fills in emailBody / emailBodyText on success.
        var generationOptions = request.getGenerationOptions();
        EmailTemplate stub = EmailTemplate.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .emailType(EmailType.STANDARD_PHISH)
                .payloadType(request.getPayloadType())
                .emailSubject(request.getEmailSubject())
                .emailBody("")
                .emailBodyText("")
                .difficultyLevel(request.getDifficultyLevel())
                .tone(generationOptions != null ? generationOptions.getTone() : null)
                .department(request.getDepartment())
                .targetIndustry(request.getTargetIndustry())
                .constraints(generationOptions != null ? generationOptions.getConstraints() : null)
                .expectedUserAction(request.getExpectedUserAction())
                .triggerEvent(request.getTriggerEvent())
                .urgencyLevel(generationOptions != null ? generationOptions.getUrgencyLevel() : null)
                .socialEngineeringStrategy(request.getSocialEngineeringStrategy())
                .emotionalTrigger(generationOptions != null ? generationOptions.getEmotionalTrigger() : null)
                .campaignObjective(request.getCampaignObjective())
                .attackerPersona(request.getAttackerPersona())
                .attackTechnique(request.getAttackTechnique())
                .brand(generationOptions != null ? generationOptions.getBrand() : null)
                .callToAction(generationOptions != null ? generationOptions.getCallToAction() : null)
                .serviceLocation(request.getServiceLocation())
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .employeeDataRequired(request.getEmployeeDataRequired() != null
                        ? new ArrayList<>(request.getEmployeeDataRequired()) : new ArrayList<>())
                .language(request.getInputLanguage() != null ? request.getInputLanguage() : "en")
                .clientId(clientId)
                .createdBy(userId)
                .createdByRole(userType != null ? userType.getValue() : null)
                .popularity(0)
                .status(EmailTemplateStatus.PROCESSING)
                .isGlobal(isGlobalTemplate(userType))
                .isPremium(false)
                .attachments(new ArrayList<>())
                .templateGenerationType(TemplateGenerationType.AI)
                .build();

        EmailTemplate savedStub = emailTemplateRepository.save(stub);

        if (request.getLandingPageIds() != null) {
            bindingService.setLandingPagesForTemplate(savedStub.getId(), request.getLandingPageIds(), clientId);
            savedStub = emailTemplateRepository.findById(savedStub.getId()).orElse(savedStub);
        }

        AiGenerationJob job = AiGenerationJob.builder()
                .jobType(AiGenerationJobType.EMAIL_TEMPLATE)
                .status(AiGenerationJobStatus.PROCESSING)
                .clientId(clientId)
                .createdBy(userId)
                .createdByRole(userType != null ? userType.getValue() : null)
                .targetEntityId(savedStub.getId())
                .emailTemplateRequest(request)
                .attempts(0)
                .build();
        AiGenerationJob savedJob = aiGenerationJobRepository.save(job);

        savedStub.setAiGenerationJobId(savedJob.getId());
        emailTemplateRepository.save(savedStub);

        try {
            aiContentGenerationSqsService.sendMessage(AiContentGenerationMessage.builder()
                    .jobId(savedJob.getId())
                    .jobType(AiGenerationJobType.EMAIL_TEMPLATE)
                    .build());
        } catch (RuntimeException e) {
            // Roll back to FAILED so the user does not see a permanent PROCESSING template
            // when we cannot enqueue the job (e.g. SQS unavailable).
            String reason = e.getMessage() != null ? e.getMessage() : "Failed to enqueue AI generation job";
            savedStub.setStatus(EmailTemplateStatus.FAILED);
            savedStub.setAiErrorMessage(reason);
            emailTemplateRepository.save(savedStub);

            savedJob.setStatus(AiGenerationJobStatus.FAILED);
            savedJob.setErrorMessage(reason);
            aiGenerationJobRepository.save(savedJob);

            log.error("Failed to enqueue AI email template generation jobId={} templateId={}",
                    savedJob.getId(), savedStub.getId(), e);
            throw e;
        }

        log.info("AI Template {} accepted (jobId={}) for user {} with userType {}",
                savedStub.getId(), savedJob.getId(), userId,
                userType != null ? userType.getValue() : null);

        return emailTemplateMapper.toDto(savedStub, true, true);
    }
    
    @Override
    public SanitizeHtmlResult sanitizeHtml(String htmlContent) {
        return htmlSanitizerService.sanitizeWithDetails(htmlContent);
    }

    @Override
    public boolean isTemplateNameUnique(String templateName) {
        String clientId = getCurrentClientId();
        return !emailTemplateRepository.existsByClientIdAndTemplateName(clientId, templateName);
    }

    /**
     * Trim, drop blanks, distinct; enforces max count after filtering.
     */
    private List<String> normalizeAttachmentUrls(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> out = raw.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (out.size() > MAX_ATTACHMENTS) {
            throw new ServiceException("Maximum " + MAX_ATTACHMENTS + " attachments allowed");
        }
        return new ArrayList<>(out);
    }
    
    // --- Permission checking methods ---
    
    /**
     * Check if user can edit the template
     * BR-09: Admin can only edit templates they created
     * BR-10: Super Admin templates cannot be edited by regular Admins
     */
    private boolean canEditTemplate(EmailTemplate template, UserType currentUserType) {
        log.info("Checking edit permission for userType {} on template {} (createdByRole: {}, isGlobal: {})",
                currentUserType != null ? currentUserType.getValue() : null,
                template.getId(), template.getCreatedByRole(), template.isGlobal());
        if (currentUserType == null) {
            return false;
        }

        // SUPER_ADMIN / ASPIRE_ADMIN can edit templates they have access to (client + global).
        if (currentUserType == UserType.SUPER_ADMIN || currentUserType == UserType.ASPIRE_ADMIN || currentUserType == UserType.SYSTEM_USER) {
            return true;
        }

        // CLIENT_ADMIN can edit only templates they created and that are not global.
        if (currentUserType == UserType.CLIENT_ADMIN) {
            return !template.isGlobal()
                    && ROLE_CLIENT_ADMIN.equalsIgnoreCase(template.getCreatedByRole())
                    && getCurrentUserId().equals(template.getCreatedBy());
        }

        return false;
    }
    
    /**
     * Check if user can delete the template
     * BR-09: Admin can only delete templates they created
     * BR-10: Super Admin templates cannot be deleted by regular Admins
     */
    private boolean canDeleteTemplate(EmailTemplate template, UserType currentUserType) {
        return canEditTemplate(template, currentUserType);
    }
    
    // --- Helper methods ---
    
    private String getCurrentClientId() {
        return userCurrentContextService.getCurrentUserContext().getClientAdminId();
    }
    
    private String getCurrentUserId() {
        return userCurrentContextService.getCurrentUserContext().getUserId();
    }
    
    private UserType getCurrentUserType() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private boolean isGlobalTemplate(UserType userType) {
        return userType == UserType.SUPER_ADMIN || userType == UserType.ASPIRE_ADMIN || userType == UserType.SYSTEM_USER;
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

}
