package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
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

import java.util.List;

/**
 * Controller interface for landing page management.
 * Based on BRD Use Case 2.1.4: Admin Manages Phishing Landing Page
 */
@Tag(name = "Landing Pages", description = "APIs for phishing landing page management")
@RequestMapping(value = WebApiUrlConstants.LANDING_PAGES_PATH, produces = "application/json")
public interface LandingPageController {
    
    @Operation(summary = "Get all landing pages", 
               description = "Retrieve list of landing pages with pagination, search, and filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Landing pages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<LandingPageDto>>>> getLandingPages(
            @Parameter(description = "Search by name or tags")
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Filter by client Admin Id")
            @RequestParam(required = false) String clientId,
            
            @Parameter(description = "Filter by page type")
            @RequestParam(required = false) LandingPageType pageType,
            
            @Parameter(description = "Filter by landing page category catalog id")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter by difficulty catalog id")
            @RequestParam(required = false) String difficulty,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) LandingPageStatus status,

            @Parameter(description = "Filter by tags (any match)")
            @RequestParam(required = false) List<String> tags,
            
            @Parameter(description = "Page offset (0-based)")
            @RequestParam(defaultValue = "0") int offset,
            
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            
            @Parameter(description = "Field to sort by (name, createdAt, popularity)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder);
    
    @Operation(summary = "Get landing page by ID", 
               description = "Retrieve a specific landing page by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Landing page retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Landing page not found")
    })
    @GetMapping("/{pageId}")
    ResponseEntity<ApiResponseDto<LandingPageDto>> getLandingPageById(
            @Parameter(description = "Landing page ID") @PathVariable String pageId);
    
    @Operation(summary = "Get landing page preview", 
               description = "Retrieve full HTML content for preview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preview retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Landing page not found")
    })
    @GetMapping("/{pageId}/preview")
    ResponseEntity<ApiResponseDto<LandingPagePreviewDto>> getLandingPagePreview(
            @Parameter(description = "Landing page ID") @PathVariable String pageId);

    @Operation(summary = "Get bound landing pages",
               description = "Retrieve landing pages bound to the given email or SMS template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bound landing pages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/{templateId}/landing-pages")
    ResponseEntity<ApiResponseDto<List<LandingPageDto>>> getBoundLandingPages(
            @Parameter(description = "Template ID") @PathVariable String templateId);

    @Operation(summary = "Get bound email templates",
               description = "Retrieve email or SMS templates bound to the given landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bound templates retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Landing page not found")
    })
    @GetMapping("/{pageId}/email-templates")
    ResponseEntity<ApiResponseDto<List<EmailTemplateDto>>> getBoundEmailTemplates(
            @Parameter(description = "Landing page ID") @PathVariable String pageId);
    
    @Operation(summary = "Update landing page", 
               description = "Update an existing landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Landing page updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Not authorized to edit this landing page"),
            @ApiResponse(responseCode = "404", description = "Landing page not found")
    })
    @PutMapping("/{pageId}")
    ResponseEntity<ApiResponseDto<LandingPageDto>> updateLandingPage(
            @Parameter(description = "Landing page ID") @PathVariable String pageId,
            @Valid @RequestBody LandingPageUpdateRequest request);
    
    @Operation(summary = "Delete landing page", 
               description = "Delete a landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Landing page deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this landing page"),
            @ApiResponse(responseCode = "404", description = "Landing page not found"),
            @ApiResponse(responseCode = "409", description = "Landing page is used by active campaigns")
    })
    @DeleteMapping("/{pageId}")
    ResponseEntity<ApiResponseDto<Void>> deleteLandingPage(
            @Parameter(description = "Landing page ID") @PathVariable String pageId);
    
    @Operation(summary = "Duplicate landing page", 
               description = "Create a copy of an existing landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Landing page duplicated successfully"),
            @ApiResponse(responseCode = "404", description = "Landing page not found")
    })
    @PostMapping("/{pageId}/duplicate")
    ResponseEntity<ApiResponseDto<LandingPageDto>> duplicateLandingPage(
            @Parameter(description = "Landing page ID") @PathVariable String pageId);
    
    // --- Task-05: Creation Endpoints ---
    
    @Operation(summary = "Create landing page", 
               description = "Create a new landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Landing page created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Page with this name already exists")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<LandingPageDto>> createLandingPage(
            @Valid @RequestBody LandingPageCreateRequest request);
    
    @Operation(summary = "Import website", 
               description = "Import a website from URL to create landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Website imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or inaccessible URL")
    })
    @PostMapping("/import")
    ResponseEntity<ApiResponseDto<ImportedSiteDto>> importWebsite(
            @Valid @RequestBody ImportSiteRequest request);
    
    @Operation(summary = "Generate landing page with AI", 
               description = "Generate a landing page using AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Landing page generated successfully"),
            @ApiResponse(responseCode = "500", description = "AI generation failed")
    })
    @PostMapping("/ai-generate")
    ResponseEntity<ApiResponseDto<LandingPageDto>> generateAILandingPage(
            @Valid @RequestBody AILandingPageRequest request);

    @Operation(summary = "Fix HTML page with AI",
            description = "Fix selected html element against full template and return updated full html page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "HTML page fixed successfully"),
            @ApiResponse(responseCode = "500", description = "AI fix failed")
    })
    @PostMapping("/fix/html-page")
    ResponseEntity<ApiResponseDto<String>> fixHtmlPage(
            @Valid @RequestBody FixHtmlPageRequest request);
    
    @Operation(summary = "Validate HTML content", 
               description = "Validate HTML content for syntax and security")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed")
    })
    @PostMapping("/validate-html")
    ResponseEntity<ApiResponseDto<ValidationResult>> validateHtml(
            @Valid @RequestBody ValidateHtmlRequest request);
    
    @Operation(summary = "Upload thumbnail", 
               description = "Upload or set thumbnail for landing page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thumbnail uploaded successfully"),
            @ApiResponse(responseCode = "404", description = "Landing page not found")
    })
    @PostMapping("/{pageId}/thumbnail")
    ResponseEntity<ApiResponseDto<String>> uploadThumbnail(
            @Parameter(description = "Landing page ID") @PathVariable String pageId,
            @RequestParam String thumbnailUrl);
}
