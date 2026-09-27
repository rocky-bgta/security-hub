package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.CampaignObjectiveCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignObjectiveUpdateRequest;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
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
 * Controller interface for configurable campaign objective catalog.
 */
@Tag(name = "Campaign Objectives", description = "APIs for managing phishing campaign objective configuration")
@RequestMapping(value = WebApiUrlConstants.CAMPAIGN_OBJECTIVES_PATH, produces = "application/json")
public interface CampaignObjectiveController {

    @Operation(summary = "Get list of campaign objectives",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign objectives retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CampaignObjectiveDto>>>> getCampaignObjectives(
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

    @Operation(summary = "Get campaign objective by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign objective retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Campaign objective not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<CampaignObjectiveDto>> getCampaignObjectiveById(
            @Parameter(description = "Campaign objective ID") @PathVariable String id
    );

    @Operation(summary = "Create campaign objective")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campaign objective created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<CampaignObjectiveDto>> createCampaignObjective(
            @Valid @RequestBody CampaignObjectiveCreateRequest request
    );

    @Operation(summary = "Update campaign objective")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign objective updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Campaign objective not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<CampaignObjectiveDto>> updateCampaignObjective(
            @Parameter(description = "Campaign objective ID") @PathVariable String id,
            @Valid @RequestBody CampaignObjectiveUpdateRequest request
    );

    @Operation(summary = "Delete campaign objective")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign objective deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Campaign objective not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteCampaignObjective(
            @Parameter(description = "Campaign objective ID") @PathVariable String id
    );
}
