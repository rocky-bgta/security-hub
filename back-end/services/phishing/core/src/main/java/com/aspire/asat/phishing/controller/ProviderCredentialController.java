package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Manages a client's third-party provider credentials (voice cloning and video
 * rendering) consumed by the deepfake generation pipeline. Secrets are stored
 * encrypted and never returned in full.
 */
@Tag(name = "Provider credentials",
        description = "Per-client CRUD for third-party deepfake provider credentials (ElevenLabs, Fish Audio, HeyGen)")
@RequestMapping(value = WebApiUrlConstants.PROVIDER_CREDENTIALS_PATH, produces = "application/json")
public interface ProviderCredentialController {

    @Operation(summary = "List provider credentials",
            description = "Paginated list scoped to the current client, with optional provider name and status filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider credentials retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ProviderCredentialDto>>>> getProviderCredentials(
            @Parameter(description = "Optional provider name filter (case-insensitive)")
            @RequestParam(required = false) String providerName,
            @Parameter(description = "Optional status filter (true = active, false = inactive, omitted = all)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page index (0-based)")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Field to sort by (providerName, category, createdAt, updatedAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get provider credential by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider credential retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider credential not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<ProviderCredentialDto>> getProviderCredentialById(
            @Parameter(description = "Provider credential ID") @PathVariable String id
    );

    @Operation(summary = "Create provider credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Provider credential created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Provider credential already exists")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ProviderCredentialDto>> createProviderCredential(
            @Valid @RequestBody ProviderCredentialCreateRequest request
    );

    @Operation(summary = "Update provider credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider credential updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Provider credential not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<ProviderCredentialDto>> updateProviderCredential(
            @Parameter(description = "Provider credential ID") @PathVariable String id,
            @Valid @RequestBody ProviderCredentialUpdateRequest request
    );

    @Operation(summary = "Delete provider credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider credential deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Provider credential not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteProviderCredential(
            @Parameter(description = "Provider credential ID") @PathVariable String id
    );

    @Operation(summary = "Set provider credential as the default for its category",
            description = "Marks this credential as the default (voice cloning or video rendering) and unsets any other default in the same category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default provider updated successfully"),
            @ApiResponse(responseCode = "404", description = "Provider credential not found")
    })
    @PatchMapping("/{id}/default")
    ResponseEntity<ApiResponseDto<ProviderCredentialDto>> setDefault(
            @Parameter(description = "Provider credential ID") @PathVariable String id
    );

    @Operation(summary = "Activate or deactivate a provider credential",
            description = "Inactive providers are never used by the deepfake pipeline")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Provider credential not found")
    })
    @PatchMapping("/{id}/status")
    ResponseEntity<ApiResponseDto<ProviderCredentialDto>> setActive(
            @Parameter(description = "Provider credential ID") @PathVariable String id,
            @Parameter(description = "Whether the provider should be active") @RequestParam boolean active
    );
}
