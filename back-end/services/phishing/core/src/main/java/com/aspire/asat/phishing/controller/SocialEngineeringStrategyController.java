package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyCreateRequest;
import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyUpdateRequest;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
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
 * Controller interface for configurable social engineering strategy catalog.
 */
@Tag(name = "Social Engineering Strategies", description = "APIs for managing phishing social engineering strategy configuration")
@RequestMapping(value = WebApiUrlConstants.SOCIAL_ENGINEERING_STRATEGIES_PATH, produces = "application/json")
public interface SocialEngineeringStrategyController {

    @Operation(summary = "Get list of social engineering strategies",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strategies retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SocialEngineeringStrategyDto>>>> getSocialEngineeringStrategies(
            @Parameter(description = "Optional search term (matches name, case-insensitive substring)")
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Filter by effective active entries (includes missing isActive in DB)")
            @RequestParam(defaultValue = "true") boolean isActive,

            @Parameter(description = "Page index (0-based), same semantics as email-templates list")
            @RequestParam(defaultValue = "0") int offset,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Field to sort by (displayOrder, name, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get social engineering strategy by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strategy retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Strategy not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<SocialEngineeringStrategyDto>> getSocialEngineeringStrategyById(
            @Parameter(description = "Strategy ID") @PathVariable String id
    );

    @Operation(summary = "Create social engineering strategy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Strategy created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<SocialEngineeringStrategyDto>> createSocialEngineeringStrategy(
            @Valid @RequestBody SocialEngineeringStrategyCreateRequest request
    );

    @Operation(summary = "Update social engineering strategy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strategy updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Strategy not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<SocialEngineeringStrategyDto>> updateSocialEngineeringStrategy(
            @Parameter(description = "Strategy ID") @PathVariable String id,
            @Valid @RequestBody SocialEngineeringStrategyUpdateRequest request
    );

    @Operation(summary = "Delete social engineering strategy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strategy deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Strategy not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteSocialEngineeringStrategy(
            @Parameter(description = "Strategy ID") @PathVariable String id
    );
}
