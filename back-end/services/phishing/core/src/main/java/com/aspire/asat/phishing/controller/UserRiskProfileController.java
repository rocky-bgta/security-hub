package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.UserRiskProfileSaveRequestDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for saving and managing UserRiskProfile.
 */
@Tag(name = "User Risk Profile", description = "APIs for creating and updating user risk profiles")
@RequestMapping(value = WebApiUrlConstants.USER_RISK_PROFILES_PATH, produces = "application/json")
public interface UserRiskProfileController {

    @Operation(summary = "Save user risk profile", description = "Creates or updates a user risk profile by clientId and userId. If a profile exists for the same clientId and userId, it is updated; otherwise a new one is created.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User risk profile saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing clientId or userId)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> save(@Valid @RequestBody UserRiskProfileSaveRequestDto request);

    @Operation(summary = "Get user risk profile by ID", description = "Retrieves a user risk profile by its id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> getById(
            @Parameter(description = "User risk profile ID") @PathVariable String id);

    @Operation(summary = "Update user risk profile by ID", description = "Updates an existing user risk profile. Only provided fields are updated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> updateById(
            @Parameter(description = "User risk profile ID") @PathVariable String id,
            @Valid @RequestBody UserRiskProfileSaveRequestDto request);

    @Operation(summary = "Get list of user risk profiles", description = "Returns paginated list with optional search (email, firstName) and filters (clientAdminId→clientId, department).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of profiles"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    ResponseEntity<AllResponseDto<List<UserRiskSummaryDto>>> getList(
            @Parameter(description = "Filter by client (clientAdminId maps to clientId)") @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Search in email and firstName") @RequestParam(required = false) String search,
            @Parameter(description = "Pagination offset") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "Sort field (e.g. riskScore, email, firstName)") @RequestParam(defaultValue = "riskScore") String sortBy,
            @Parameter(description = "Sort order: asc or desc") @RequestParam(defaultValue = "desc") String sortOrder);

    @Operation(summary = "Export user risk profiles as CSV",
            description = "Exports UserRiskProfile list in CSV using same filters as list API. If only clientAdminId is passed, exports all profiles for that client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV exported successfully"),
            @ApiResponse(responseCode = "400", description = "clientAdminId is required"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping(value = "/export", produces = "text/csv")
    ResponseEntity<byte[]> exportCsv(
            @Parameter(description = "Filter by client (clientAdminId maps to clientId)") @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Search in email and firstName") @RequestParam(required = false) String search);
}
