package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.SenderProfileImportResultDto;
import com.aspire.asat.phishing.dto.response.SenderProfileDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller interface for sender profile management endpoints.
 */
@Tag(name = "Sender Profiles", description = "APIs for managing sender profiles")
@RequestMapping(value = WebApiUrlConstants.SENDER_PROFILES_PATH)
public interface SenderProfileController {

    @Operation(summary = "Get all sender profiles", 
               description = "Retrieves paginated list of sender profiles with optional search and filter")
    @GetMapping
    ResponseEntity<AllResponseDto<List<SenderProfileDto>>> getSenderProfiles(
            @Parameter(description = "Pagination offset") 
            @RequestParam(defaultValue = "0") int offset,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "10") int pageSize,
            
            @Parameter(description = "Search keyword (name, email, host)") 
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Optional clientId filter; when provided returns that client plus global profiles")
            @RequestParam(required = false) String clientId,
            
            @Parameter(description = "Filter by profile type (MANAGED, CUSTOM)") 
            @RequestParam(required = false) ProfileType profileType,

            @Parameter(description = "Filter by SMTP verification status (omit for no filter)")
            @RequestParam(required = false) Boolean isVerified,

            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter by target industry id")
            @RequestParam(required = false) String targetIndustryId,

            @Parameter(description = "Filter by region id")
            @RequestParam(required = false) String regionId,

            @Parameter(description = "Filter by language")
            @RequestParam(required = false) String language,

            @Parameter(description = "Filter by deception level catalog id")
            @RequestParam(required = false) String deceptionLevel,

            @Parameter(description = "Filter by psychological triggers (any match)")
            @RequestParam(required = false) List<String> psychologicalTriggers,

            @Parameter(description = "Filter by domain type")
            @RequestParam(required = false) DomainType domainType,

            @Parameter(description = "Filter by personalization level catalog id")
            @RequestParam(required = false) String personalizationLevel,

            @Parameter(description = "Filter by provider type")
            @RequestParam(required = false) ProviderType providerType,

            @Parameter(description = "Filter by tags (any match)")
            @RequestParam(required = false) List<String> tags,
            
            @Parameter(description = "Sort by field") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort order (asc, desc)") 
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get sender profile by ID", 
               description = "Retrieves a specific sender profile by its ID")
    @GetMapping("/{profileId}")
    ResponseEntity<ApiResponseDto<SenderProfileDto>> getSenderProfileById(
            @Parameter(description = "Profile ID") @PathVariable String profileId
    );

    @Operation(summary = "Create sender profile", 
               description = "Creates a new sender profile with SMTP configuration")
    @PostMapping
    ResponseEntity<ApiResponseDto<SenderProfileDto>> createSenderProfile(
            @Valid @RequestBody SenderProfileRequest request
    );

    @Operation(summary = "Update sender profile", 
               description = "Updates an existing sender profile")
    @PutMapping("/{profileId}")
    ResponseEntity<ApiResponseDto<SenderProfileDto>> updateSenderProfile(
            @Parameter(description = "Profile ID") @PathVariable String profileId,
            @Valid @RequestBody SenderProfileRequest request
    );

    @Operation(summary = "Delete sender profile", 
               description = "Deletes a sender profile (cannot delete managed profiles or profiles in use)")
    @DeleteMapping("/{profileId}")
    ResponseEntity<ApiResponseDto<String>> deleteSenderProfile(
            @Parameter(description = "Profile ID") @PathVariable String profileId
    );

    @Operation(summary = "Duplicate sender profile", 
               description = "Creates a copy of an existing sender profile")
    @PostMapping("/{profileId}/duplicate")
    ResponseEntity<ApiResponseDto<SenderProfileDto>> duplicateSenderProfile(
            @Parameter(description = "Profile ID") @PathVariable String profileId
    );

    @Operation(summary = "Test existing profile connection", 
               description = "Tests SMTP connection for an existing profile and updates verification status")
    @PostMapping("/{profileId}/test")
    ResponseEntity<ApiResponseDto<TestResultDto>> testProfileConnection(
            @Parameter(description = "Profile ID") @PathVariable String profileId
    );

    @Operation(summary = "Test new configuration", 
               description = "Tests SMTP connection with provided configuration before saving")
    @PostMapping("/test")
    ResponseEntity<ApiResponseDto<TestResultDto>> testNewConnection(
            @Valid @RequestBody SenderProfileRequest request
    );

    @Operation(summary = "Get verified profiles", 
               description = "Retrieves all sender profiles with verified SMTP connection")
    @GetMapping("/verified")
    ResponseEntity<ApiResponseDto<java.util.List<SenderProfileDto>>> getVerifiedProfiles();

    @Operation(summary = "Check domain verification", 
               description = "Checks if the domain from an email address is verified")
    @GetMapping("/check-domain")
    ResponseEntity<ApiResponseDto<Boolean>> checkDomainVerification(
            @Parameter(description = "Email address to check") @RequestParam String email
    );

    @Operation(summary = "Import sender profiles",
               description = "Bulk import sender profiles from CSV file")
    @PostMapping("/import")
    ResponseEntity<ApiResponseDto<SenderProfileImportResultDto>> importSenderProfiles(
            @Parameter(description = "Sender profile CSV file") @RequestParam("file") MultipartFile file
    );
}
