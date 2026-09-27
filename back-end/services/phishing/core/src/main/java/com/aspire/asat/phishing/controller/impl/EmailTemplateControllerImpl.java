package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.phishing.controller.EmailTemplateController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.request.EmailTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.EmailTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.request.FixEmailTemplateRequest;
import com.aspire.asat.phishing.dto.request.SanitizeHtmlRequest;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.EmailTemplatePreviewDto;
import com.aspire.asat.phishing.dto.response.FilterOptionsDto;
import com.aspire.asat.phishing.dto.response.SanitizeHtmlResult;
import com.aspire.asat.phishing.dto.response.TranscriptionResult;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.AIContentGeneratorService;
import com.aspire.asat.phishing.service.EmailTemplateService;
import com.aspire.asat.phishing.service.VoiceTranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EmailTemplateController.
 * Handles HTTP requests for email template management.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateControllerImpl implements EmailTemplateController {
    
    private final EmailTemplateService emailTemplateService;
    private final AIContentGeneratorService aiContentGeneratorService;
    private final VoiceTranscriptionService voiceTranscriptionService;
    private final MessageService messageService;
    
    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EmailTemplateDto>>>> getTemplates(
            String searchParam,
            String clientId,
            String difficultyLevel,
            String payloadType,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            log.info("Getting templates with search: {}, offset: {}, pageSize: {}", 
                    searchParam, offset, pageSize);
            
            List<EmailTemplateDto> templates = emailTemplateService.getTemplates(
                    searchParam, difficultyLevel, payloadType, location, tags, language, status, templateType, clientId,
                    offset, pageSize, sortBy, sortOrder);
            
            long total = emailTemplateService.countTemplates(
                    searchParam, difficultyLevel, payloadType, location, tags, language, status, templateType, clientId);
            
            AllResponseDto<List<EmailTemplateDto>> response = 
                    new AllResponseDto<>(offset, pageSize, total, templates);
            
            return ResponseEntity.ok(new ApiResponseDto<>("Templates retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting templates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve templates", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplateDto>> getTemplateById(String templateId) {
        try {
            log.info("Getting template by ID: {}", templateId);
            
            return emailTemplateService.getTemplateById(templateId)
                    .map(template -> ResponseEntity.ok(
                            new ApiResponseDto<>("Template retrieved successfully", 200, template)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>("Template not found", 404, null)));
        } catch (Exception e) {
            log.error("Error getting template by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve template", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplatePreviewDto>> getTemplatePreview(String templateId) {
        try {
            log.info("Getting template preview for ID: {}", templateId);
            
            return emailTemplateService.getTemplatePreview(templateId)
                    .map(preview -> ResponseEntity.ok(
                            new ApiResponseDto<>("Preview retrieved successfully", 200, preview)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>("Template not found", 404, null)));
        } catch (Exception e) {
            log.error("Error getting template preview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve preview", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplateDto>> updateTemplate(
            String templateId, EmailTemplateUpdateRequest request) {
        try {
            log.info("Updating template: {}", templateId);
            
            EmailTemplateDto updated = emailTemplateService.updateTemplate(templateId, request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.TEMPLATE_UPDATED), 200, updated));
        } catch (ResourceNotFoundException e) {
            log.warn("Template not found: {}", templateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (ServiceException e) {
            log.warn("Service error updating template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(e.getMessage(), 401, null));
        } catch (Exception e) {
            log.error("Error updating template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update template", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteTemplate(String templateId) {
        try {
            log.info("Deleting template: {}", templateId);
            
            emailTemplateService.deleteTemplate(templateId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.TEMPLATE_DELETED), 200, null));
        } catch (ResourceNotFoundException e) {
            log.warn("Template not found: {}", templateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (ServiceException e) {
            log.warn("Service error deleting template: {}", e.getMessage());
            
            if (e.getMessage().contains("active campaigns")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(e.getMessage(), 409, null));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(e.getMessage(), 401, null));
        } catch (Exception e) {
            log.error("Error deleting template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to delete template", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplateDto>> duplicateTemplate(String templateId) {
        try {
            log.info("Duplicating template: {}", templateId);
            
            EmailTemplateDto duplicated = emailTemplateService.duplicateTemplate(templateId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.TEMPLATE_DUPLICATED), 200, duplicated));
        } catch (ResourceNotFoundException e) {
            log.warn("Template not found: {}", templateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error duplicating template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to duplicate template", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> getAvailableTags() {
        try {
            log.info("Getting available tags");
            
            List<String> tags = emailTemplateService.getAvailableTags();
            return ResponseEntity.ok(new ApiResponseDto<>("Tags retrieved successfully", 200, tags));
        } catch (Exception e) {
            log.error("Error getting tags: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve tags", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<FilterOptionsDto>> getFilterOptions(PayloadTypeChannel channel) {
        try {
            log.info("Getting filter options");

            FilterOptionsDto options = emailTemplateService.getFilterOptions(channel);
            return ResponseEntity.ok(new ApiResponseDto<>("Filter options retrieved successfully", 200, options));
        } catch (Exception e) {
            log.error("Error getting filter options: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve filter options", 500, null));
        }
    }
    
    // --- Task-03: Template Creation Endpoints ---
    
    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplateDto>> createTemplate(EmailTemplateCreateRequest request) {
        try {
            log.info("Creating new template: {}", request.getTemplateName());
            
            EmailTemplateDto created = emailTemplateService.createTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>(messageService.get(MessageKeys.TEMPLATE_CREATED), 201, created));
        } catch (ServiceException e) {
            log.warn("Service error creating template: {}", e.getMessage());
            
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(e.getMessage(), 409, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error creating template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to create template", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplateDto>> generateAITemplate(AITemplateGenerateRequest request) {
        try {
            log.info("Generating AI template: {} with persona: {}", 
                    request.getTemplateName(), request.getAttackerPersona());
            
            EmailTemplateDto generated = emailTemplateService.generateAITemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>(messageService.get(MessageKeys.TEMPLATE_GENERATED), 201, generated));
        } catch (ServiceException e) {
            log.warn("Service error generating AI template: {}", e.getMessage());
            
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(e.getMessage(), 409, null));
            }
            if (e.getMessage().contains("AI content generation failed")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponseDto<>(e.getMessage(), 500, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error generating AI template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI content generation failed: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> fixEmailTemplate(FixEmailTemplateRequest request) {
        try {
            log.info("Fixing html email template using provider={}", request.getProviderType());
            String fixedHtml = aiContentGeneratorService.fixEmailTemplate(request);
            return ResponseEntity.ok(new ApiResponseDto<>("HTML email template fixed successfully", 200, fixedHtml));
        } catch (ServiceException e) {
            log.warn("Service error fixing html email template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI fix failed: " + e.getMessage(), 500, null));
        } catch (Exception e) {
            log.error("Error fixing html email template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI fix failed", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<TranscriptionResult>> transcribeVoice(
            MultipartFile audioFile, String languageHint) {
        try {
            log.info("Transcribing voice, file: {}, language hint: {}", 
                    audioFile.getOriginalFilename(), languageHint);
            
            TranscriptionResult result = voiceTranscriptionService.transcribe(audioFile, languageHint);
            return ResponseEntity.ok(new ApiResponseDto<>("Transcription successful", 200, result));
        } catch (ServiceException e) {
            log.warn("Service error transcribing voice: {}", e.getMessage());
            
            if (e.getMessage().contains("not supported")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponseDto<>(e.getMessage(), 400, null));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(e.getMessage(), 500, null));
        } catch (Exception e) {
            log.error("Error transcribing voice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Voice transcription failed. Please try again", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<SanitizeHtmlResult>> sanitizeHtml(SanitizeHtmlRequest request) {
        try {
            log.info("Sanitizing HTML content");
            
            SanitizeHtmlResult result = emailTemplateService.sanitizeHtml(request.getHtmlContent());
            return ResponseEntity.ok(new ApiResponseDto<>("HTML sanitized successfully", 200, result));
        } catch (Exception e) {
            log.error("Error sanitizing HTML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to sanitize HTML", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<Map<String, List<String>>>> getAIGenerationOptions() {
        try {
            log.info("Getting AI generation options");
            
            Map<String, List<String>> options = new HashMap<>();
            options.put("targetIndustries", aiContentGeneratorService.getTargetIndustries());
            options.put("attackerPersonas", aiContentGeneratorService.getAttackerPersonas());
            options.put("socialEngineeringStrategies", aiContentGeneratorService.getSocialEngineeringStrategies());
            options.put("campaignObjectives", aiContentGeneratorService.getCampaignObjectives());
            
            return ResponseEntity.ok(new ApiResponseDto<>("AI options retrieved successfully", 200, options));
        } catch (Exception e) {
            log.error("Error getting AI options: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve AI options", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<Map<String, String>>> getSupportedLanguages() {
        try {
            log.info("Getting supported languages");
            
            Map<String, String> languages = voiceTranscriptionService.getSupportedLanguages();
            return ResponseEntity.ok(new ApiResponseDto<>("Languages retrieved successfully", 200, languages));
        } catch (Exception e) {
            log.error("Error getting languages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve languages", 500, null));
        }
    }
}
