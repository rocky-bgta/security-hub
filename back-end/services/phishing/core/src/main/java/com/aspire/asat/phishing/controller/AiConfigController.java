package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.AiGlobalConfigRequest;
import com.aspire.asat.phishing.dto.response.AiGlobalConfigResponse;
import com.aspire.asat.phishing.model.AiProviderSecretRef;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Global AI provider configuration (SSM + DB metadata).
 */
@Tag(name = "AI configuration", description = "Configure AI provider credentials per tenant")
@RequestMapping(value = WebApiUrlConstants.AI_CONFIG_PATH, produces = "application/json")
public interface AiConfigController {

    @Operation(summary = "List AI provider secret references",
            description = "Returns metadata rows from ai_provider_secret_refs for the current tenant clientAdminId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<List<AiProviderSecretRef>>> listAiProviderSecretRefs();

    @Operation(summary = "Save global AI provider credentials",
            description = "Stores API key (and optional secret) in AWS Parameter Store and upserts ai_provider_secret_refs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration saved"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<AiGlobalConfigResponse>> saveGlobalConfig(
            @Valid @RequestBody AiGlobalConfigRequest request);
}
