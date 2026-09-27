package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller interface for email template management.
 * Based on BRD Use Case 2.1.3: Admin Creates and Manages Phishing Email Templates
 */
@Tag(name = "Email Templates", description = "APIs for phishing email template management")
@RequestMapping(value = WebApiUrlConstants.EMAIL_TEMPLATES_PATH, produces = "application/json")
public interface EmailTemplateController {
    
    @Operation(summary = "Get all email templates", 
               description = "Retrieve list of email templates with pagination, search, and filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EmailTemplateDto>>>> getTemplates(
            @Parameter(description = "Search by name, subject, or tags")
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Filter by client Admin Id")
            @RequestParam(required = false) String clientId,
            
            @Parameter(description = "Filter by difficulty level")
            @RequestParam(required = false) String difficultyLevel,
            
            @Parameter(description = "Filter by payload type")
            @RequestParam(required = false) String payloadType,
            
            @Parameter(description = "Filter by service location")
            @RequestParam(required = false) String location,
            
            @Parameter(description = "Filter by tags (comma-separated)")
            @RequestParam(required = false) List<String> tags,
            
            @Parameter(description = "Filter by language")
            @RequestParam(required = false) String language,

            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, DRAFT)")
            @RequestParam(required = false) EmailTemplateStatus status,

            @Parameter(description = "Filter by template type (EMAIL, SMS)")
            @RequestParam(required = false) TemplateType templateType,
            
            @Parameter(description = "Page offset (0-based)")
            @RequestParam(defaultValue = "0") int offset,
            
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            
            @Parameter(description = "Field to sort by (templateName, createdAt, popularity)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder);
    
    @Operation(summary = "Get template by ID", description = "Retrieve a specific email template by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/{templateId}")
    ResponseEntity<ApiResponseDto<EmailTemplateDto>> getTemplateById(
            @Parameter(description = "Template ID") @PathVariable String templateId);
    
    @Operation(summary = "Get template preview", 
               description = "Retrieve full template content for preview (email HTML or SMS body)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preview retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/{templateId}/preview")
    ResponseEntity<ApiResponseDto<EmailTemplatePreviewDto>> getTemplatePreview(
            @Parameter(description = "Template ID") @PathVariable String templateId);
    
    @Operation(summary = "Update template", description = "Update an existing email template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Not authorized to edit this template"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PutMapping("/{templateId}")
    ResponseEntity<ApiResponseDto<EmailTemplateDto>> updateTemplate(
            @Parameter(description = "Template ID") @PathVariable String templateId,
            @Valid @RequestBody EmailTemplateUpdateRequest request);
    
    @Operation(summary = "Delete template", description = "Delete an email template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this template"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "409", description = "Template is used by active campaigns")
    })
    @DeleteMapping("/{templateId}")
    ResponseEntity<ApiResponseDto<Void>> deleteTemplate(
            @Parameter(description = "Template ID") @PathVariable String templateId);
    
    @Operation(summary = "Duplicate template", 
               description = "Create a copy of an existing email template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template duplicated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PostMapping("/{templateId}/duplicate")
    ResponseEntity<ApiResponseDto<EmailTemplateDto>> duplicateTemplate(
            @Parameter(description = "Template ID") @PathVariable String templateId);
    
    @Operation(summary = "Get available tags", description = "Get list of predefined template tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
    })
    @GetMapping("/tags")
    ResponseEntity<ApiResponseDto<List<String>>> getAvailableTags();
    
    @Operation(summary = "Get filter options", 
               description = "Get available filter options for template list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filter options retrieved successfully")
    })
    @GetMapping("/filters")
    ResponseEntity<ApiResponseDto<FilterOptionsDto>> getFilterOptions(
            @Parameter(description = "Payload type channel for filter dropdown (EMAIL or SMS). Defaults to EMAIL.")
            @RequestParam(required = false, defaultValue = "EMAIL") PayloadTypeChannel channel);
    
    // --- Task-03: Template Creation Endpoints ---
    
    @Operation(summary = "Create new template", 
               description = "Create a new email template manually")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Template name already exists")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<EmailTemplateDto>> createTemplate(
            @Valid @RequestBody EmailTemplateCreateRequest request);
    
    @Operation(summary = "Generate template with AI", 
               description = "Generate email template content using AI with context parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Template generated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Template name already exists"),
            @ApiResponse(responseCode = "500", description = "AI generation failed")
    })
    @PostMapping("/ai-generate")
    ResponseEntity<ApiResponseDto<EmailTemplateDto>> generateAITemplate(
            @Valid @RequestBody AITemplateGenerateRequest request);

    @Operation(summary = "Fix email template with AI",
            description = "Fix selected html element against full template and return updated full html email template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "HTML email template fixed successfully"),
            @ApiResponse(responseCode = "500", description = "AI fix failed")
    })
    @PostMapping("/fix/email-template")
    ResponseEntity<ApiResponseDto<String>> fixEmailTemplate(
            @Valid @RequestBody FixEmailTemplateRequest request);
    
    @Operation(summary = "Transcribe voice to text", 
               description = "Transcribe audio file to text for template creation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transcription successful"),
            @ApiResponse(responseCode = "400", description = "Invalid audio file or unsupported language"),
            @ApiResponse(responseCode = "500", description = "Transcription failed")
    })
    @PostMapping("/transcribe")
    ResponseEntity<ApiResponseDto<TranscriptionResult>> transcribeVoice(
            @RequestPart("file") MultipartFile audioFile,
            @RequestParam(required = false) String languageHint);
    
    @Operation(summary = "Sanitize HTML content", 
               description = "Sanitize HTML content by removing dangerous elements and attributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "HTML sanitized successfully")
    })
    @PostMapping("/sanitize-html")
    ResponseEntity<ApiResponseDto<SanitizeHtmlResult>> sanitizeHtml(
            @Valid @RequestBody SanitizeHtmlRequest request);
    
    @Operation(summary = "Get AI generation options", 
               description = "Get available options for AI template generation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Options retrieved successfully")
    })
    @GetMapping("/ai-options")
    ResponseEntity<ApiResponseDto<Map<String, List<String>>>> getAIGenerationOptions();
    
    @Operation(summary = "Get supported languages", 
               description = "Get supported languages for voice input")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Languages retrieved successfully")
    })
    @GetMapping("/languages")
    ResponseEntity<ApiResponseDto<Map<String, String>>> getSupportedLanguages();
}
