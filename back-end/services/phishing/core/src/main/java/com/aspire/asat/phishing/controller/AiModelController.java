package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiModelCreateRequest;
import com.aspire.asat.phishing.dto.request.AiModelUpdateRequest;
import com.aspire.asat.phishing.dto.response.AiModelDto;
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

@Tag(name = "AI models", description = "Catalog of AI models per provider")
@RequestMapping(value = WebApiUrlConstants.AI_MODELS_PATH, produces = "application/json")
public interface AiModelController {

    @Operation(summary = "Create AI model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<AiModelDto>> createAiModel(@Valid @RequestBody AiModelCreateRequest request);

    @Operation(summary = "Get AI model by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<AiModelDto>> getAiModelById(
            @Parameter(description = "AI model id") @PathVariable String id);

    @Operation(summary = "Update AI model", description = "Update model name and provider type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<AiModelDto>> updateAiModel(
            @Parameter(description = "AI model id") @PathVariable String id,
            @Valid @RequestBody AiModelUpdateRequest request);

    @Operation(summary = "List AI models", description = "Optional filter by provider type")
    @GetMapping
    ResponseEntity<ApiResponseDto<List<AiModelDto>>> listAiModels(
            @Parameter(description = "Filter by provider (omit for all)")
            @RequestParam(required = false) AiProviderType providerType);

    @Operation(summary = "Delete AI model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteAiModel(
            @Parameter(description = "AI model id") @PathVariable String id);
}
