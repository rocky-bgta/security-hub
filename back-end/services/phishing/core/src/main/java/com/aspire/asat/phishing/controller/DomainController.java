package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.dto.request.AdminDomainAddRequest;
import com.aspire.asat.phishing.dto.request.DomainVerificationRequest;
import com.aspire.asat.phishing.dto.request.GenerateVerificationRequest;
import com.aspire.asat.phishing.dto.response.DomainDto;
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
 * Controller interface for domain verification and locking operations.
 * Based on BRD Use Case 2.1.1.1: Aspire Admin Verifies and Locks a Domain
 */
@Tag(name = "Domain Verification", description = "APIs for domain verification and locking management")
@RequestMapping(value = WebApiUrlConstants.DOMAINS_PATH, produces = "application/json")
public interface DomainController {
    
    @Operation(summary = "Get all domains", description = "Retrieve list of domains with pagination and search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domains retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<DomainDto>>>> getDomains(
            @Parameter(description = "Search term for domain name") 
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by one or more domain statuses (repeat param for multiple values)")
            @RequestParam(required = false) List<DomainStatus> status,
            
            @Parameter(description = "Page offset (0-based)") 
            @RequestParam(defaultValue = "0") int offset,
            
            @Parameter(description = "Number of items per page") 
            @RequestParam(defaultValue = "10") int pageSize,
            
            @Parameter(description = "Field to sort by") 
            @RequestParam(defaultValue = "domain") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)") 
            @RequestParam(defaultValue = "asc") String sortDirection);
    
    @Operation(summary = "Get domain by ID", description = "Retrieve a specific domain by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    @GetMapping("/{domainId}")
    ResponseEntity<ApiResponseDto<DomainDto>> getDomainById(
            @Parameter(description = "Domain ID") @PathVariable String domainId);
    
    @Operation(summary = "Generate verification email", 
               description = "Generate and send a verification code to the provided email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email or validation error"),
            @ApiResponse(responseCode = "409", description = "Domain locked by another tenant"),
            @ApiResponse(responseCode = "429", description = "Too many verification attempts")
    })
    @PostMapping(WebApiUrlConstants.DOMAIN_GENERATE_VERIFICATION)
    ResponseEntity<ApiResponseDto<String>> generateVerificationEmail(
            @Valid @RequestBody GenerateVerificationRequest request);
    
    @Operation(summary = "Verify domain", 
               description = "Verify domain ownership using the verification code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired verification code")
    })
    @PostMapping(WebApiUrlConstants.DOMAIN_VERIFY)
    ResponseEntity<ApiResponseDto<DomainDto>> verifyDomain(
            @Valid @RequestBody DomainVerificationRequest request);

    @Operation(summary = "Admin: add or verify domain",
            description = "Aspire admin only. Saves the domain as VERIFIED if new; otherwise sets status to VERIFIED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain saved as verified"),
            @ApiResponse(responseCode = "400", description = "Invalid domain"),
            @ApiResponse(responseCode = "403", description = "Not an Aspire administrator")
    })
    @PostMapping(WebApiUrlConstants.DOMAIN_ADMIN_ADD)
    ResponseEntity<ApiResponseDto<DomainDto>> adminAddVerifiedDomain(
            @Valid @RequestBody AdminDomainAddRequest request);
    
    @Operation(summary = "Lock domain", 
               description = "Lock a verified domain to prevent other tenants from verifying it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain locked successfully"),
            @ApiResponse(responseCode = "403", description = "Locking not available for subscription tier"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    @PutMapping(WebApiUrlConstants.DOMAIN_LOCK)
    ResponseEntity<ApiResponseDto<DomainDto>> lockDomain(
            @Parameter(description = "Domain ID") @PathVariable String domainId);
    
    @Operation(summary = "Unlock domain", description = "Unlock a previously locked domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain unlocked successfully"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    @PutMapping(WebApiUrlConstants.DOMAIN_UNLOCK)
    ResponseEntity<ApiResponseDto<DomainDto>> unlockDomain(
            @Parameter(description = "Domain ID") @PathVariable String domainId);
    
    @Operation(summary = "Delete domain", description = "Delete a domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this domain"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    @DeleteMapping("/{domainId}")
    ResponseEntity<ApiResponseDto<Void>> deleteDomain(
            @Parameter(description = "Domain ID") @PathVariable String domainId);
    
    @Operation(summary = "Resend verification email", 
               description = "Resend the verification code to the email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email resent successfully"),
            @ApiResponse(responseCode = "429", description = "Too many verification attempts")
    })
    @PostMapping("/resend-verification-email")
    ResponseEntity<ApiResponseDto<String>> resendVerificationEmail(
            @Valid @RequestBody GenerateVerificationRequest request);
}
