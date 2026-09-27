package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.phishing.controller.LandingPageController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.request.AILandingPageRequest;
import com.aspire.asat.phishing.dto.request.FixHtmlPageRequest;
import com.aspire.asat.phishing.dto.request.ImportSiteRequest;
import com.aspire.asat.phishing.dto.request.LandingPageCreateRequest;
import com.aspire.asat.phishing.dto.request.LandingPageUpdateRequest;
import com.aspire.asat.phishing.dto.request.ValidateHtmlRequest;
import com.aspire.asat.phishing.dto.response.ImportedSiteDto;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.dto.response.LandingPagePreviewDto;
import com.aspire.asat.phishing.dto.response.ValidationResult;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.AILandingPageService;
import com.aspire.asat.phishing.service.LandingPageService;
import com.aspire.asat.phishing.service.WebsiteImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of LandingPageController.
 * Handles HTTP requests for landing page management.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class LandingPageControllerImpl implements LandingPageController {
    
    private final LandingPageService landingPageService;
    private final WebsiteImportService websiteImportService;
    private final AILandingPageService aiLandingPageService;
    private final MessageService messageService;
    
    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<LandingPageDto>>>> getLandingPages(
            String searchParam,
            String clientId,
            LandingPageType pageType,
            String category,
            String difficulty,
            LandingPageStatus status,
            List<String> tags,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            log.info("Getting landing pages with search: {}, offset: {}, pageSize: {}", 
                    searchParam, offset, pageSize);
            
            List<LandingPageDto> pages = landingPageService.getLandingPages(
                    searchParam, pageType, category, difficulty, status, tags, clientId,
                    offset, pageSize, sortBy, sortOrder);

            long total = landingPageService.countLandingPages(
                    searchParam,
                    pageType,
                    category,
                    difficulty,
                    status,
                    tags,
                    clientId
            );
            
            AllResponseDto<List<LandingPageDto>> response = 
                    new AllResponseDto<>(offset, pageSize, total, pages);
            
            return ResponseEntity.ok(new ApiResponseDto<>("Landing pages retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting landing pages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve landing pages", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<LandingPageDto>> getLandingPageById(String pageId) {
        try {
            log.info("Getting landing page by ID: {}", pageId);
            
            return landingPageService.getLandingPageById(pageId)
                    .map(page -> ResponseEntity.ok(
                            new ApiResponseDto<>("Landing page retrieved successfully", 200, page)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>("Landing page not found", 404, null)));
        } catch (Exception e) {
            log.error("Error getting landing page by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve landing page", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<LandingPagePreviewDto>> getLandingPagePreview(String pageId) {
        try {
            log.info("Getting landing page preview for ID: {}", pageId);
            
            return landingPageService.getLandingPagePreview(pageId)
                    .map(preview -> ResponseEntity.ok(
                            new ApiResponseDto<>("Preview retrieved successfully", 200, preview)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>("Landing page not found", 404, null)));
        } catch (Exception e) {
            log.error("Error getting landing page preview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve preview", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<LandingPageDto>>> getBoundLandingPages(String templateId) {
        try {
            log.info("Getting bound landing pages for template ID: {}", templateId);
            List<LandingPageDto> landingPages = landingPageService.getBoundLandingPages(templateId);
            return ResponseEntity.ok(new ApiResponseDto<>("Bound landing pages retrieved successfully", 200, landingPages));
        } catch (ResourceNotFoundException e) {
            log.warn("Template not found: {}", templateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting bound landing pages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve bound landing pages", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<EmailTemplateDto>>> getBoundEmailTemplates(String pageId) {
        try {
            log.info("Getting bound email templates for landing page ID: {}", pageId);
            List<EmailTemplateDto> templates = landingPageService.getBoundEmailTemplates(pageId);
            return ResponseEntity.ok(new ApiResponseDto<>("Bound templates retrieved successfully", 200, templates));
        } catch (ResourceNotFoundException e) {
            log.warn("Landing page not found: {}", pageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting bound email templates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve bound templates", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<LandingPageDto>> updateLandingPage(
            String pageId, LandingPageUpdateRequest request) {
        try {
            log.info("Updating landing page: {}", pageId);
            
            LandingPageDto updated = landingPageService.updateLandingPage(pageId, request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.LANDING_PAGE_UPDATED), 200, updated));
        } catch (ResourceNotFoundException e) {
            log.warn("Landing page not found: {}", pageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (ServiceException e) {
            log.warn("Service error updating landing page: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(e.getMessage(), 401, null));
        } catch (Exception e) {
            log.error("Error updating landing page: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update landing page", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteLandingPage(String pageId) {
        try {
            log.info("Deleting landing page: {}", pageId);
            
            landingPageService.deleteLandingPage(pageId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.LANDING_PAGE_DELETED), 200, null));
        } catch (ResourceNotFoundException e) {
            log.warn("Landing page not found: {}", pageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (ServiceException e) {
            log.warn("Service error deleting landing page: {}", e.getMessage());
            
            if (e.getMessage().contains("active campaigns")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(e.getMessage(), 409, null));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(e.getMessage(), 401, null));
        } catch (Exception e) {
            log.error("Error deleting landing page: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to delete landing page", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<LandingPageDto>> duplicateLandingPage(String pageId) {
        try {
            log.info("Duplicating landing page: {}", pageId);
            
            LandingPageDto duplicated = landingPageService.duplicateLandingPage(pageId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.LANDING_PAGE_DUPLICATED), 200, duplicated));
        } catch (ResourceNotFoundException e) {
            log.warn("Landing page not found: {}", pageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error duplicating landing page: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to duplicate landing page", 500, null));
        }
    }
    
    // --- Task-05: Creation Endpoints ---
    
    @Override
    public ResponseEntity<ApiResponseDto<LandingPageDto>> createLandingPage(LandingPageCreateRequest request) {
        try {
            log.info("Creating landing page: {}", request.getName());
            
            LandingPageDto created = landingPageService.createLandingPage(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>(messageService.get(MessageKeys.LANDING_PAGE_CREATED), 201, created));
        } catch (ServiceException e) {
            log.warn("Service error creating landing page: {}", e.getMessage());
            
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(e.getMessage(), 409, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error creating landing page: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to create landing page", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<ImportedSiteDto>> importWebsite(ImportSiteRequest request) {
        try {
            log.info("Importing website from URL: {}", request.getWebsiteUrl());
            
            ImportedSiteDto imported = websiteImportService.importWebsite(request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.IMPORT_WEBSITE_SUCCESS), 200, imported));
        } catch (ServiceException e) {
            log.warn("Service error importing website: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error importing website: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to import website", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<LandingPageDto>> generateAILandingPage(AILandingPageRequest request) {
        try {
            String generationMode = request.getGenerationMode() != null
                    ? request.getGenerationMode()
                    : null;
            log.info("Generating AI landing page: mode={}", generationMode != null ? generationMode : "");
            
            LandingPageDto generated = aiLandingPageService.generateLandingPage(request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.LANDING_PAGE_GENERATED), 200, generated));
        } catch (ServiceException e) {
            log.warn("Service error generating AI landing page: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI generation failed: " + e.getMessage(), 500, null));
        } catch (Exception e) {
            log.error("Error generating AI landing page: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI generation failed: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> fixHtmlPage(FixHtmlPageRequest request) {
        try {
            log.info("Fixing html page using provider={}", request.getProviderType());
            String fixedHtml = aiLandingPageService.fixHtmlPage(request);
            return ResponseEntity.ok(new ApiResponseDto<>("HTML page fixed successfully", 200, fixedHtml));
        } catch (ServiceException e) {
            log.warn("Service error fixing html page: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI fix failed: " + e.getMessage(), 500, null));
        } catch (Exception e) {
            log.error("Error fixing html page: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("AI fix failed: " + e.getMessage(), 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<ValidationResult>> validateHtml(ValidateHtmlRequest request) {
        try {
            log.info("Validating HTML content");
            
            ValidationResult result = landingPageService.validateHtml(
                    request.getHtmlContent(), 
                    request.isValidateForms(), 
                    request.isSecurityCheck());
            return ResponseEntity.ok(new ApiResponseDto<>("Validation completed", 200, result));
        } catch (Exception e) {
            log.error("Error validating HTML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to validate HTML", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<String>> uploadThumbnail(String pageId, String thumbnailUrl) {
        try {
            log.info("Uploading thumbnail for landing page: {}", pageId);
            
            String result = landingPageService.uploadThumbnail(pageId, thumbnailUrl);
            return ResponseEntity.ok(new ApiResponseDto<>("Thumbnail uploaded successfully", 200, result));
        } catch (ResourceNotFoundException e) {
            log.warn("Landing page not found: {}", pageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error uploading thumbnail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to upload thumbnail", 500, null));
        }
    }
}
