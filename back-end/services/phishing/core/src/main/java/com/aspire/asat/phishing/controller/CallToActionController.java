package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.CallToActionCreateRequest;
import com.aspire.asat.phishing.dto.request.CallToActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
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
 * Controller interface for configurable call-to-action catalog.
 */
@Tag(name = "Call to actions", description = "APIs for managing phishing call-to-action configuration")
@RequestMapping(value = WebApiUrlConstants.CALL_TO_ACTIONS_PATH, produces = "application/json")
public interface CallToActionController {

    @Operation(summary = "Get list of call to actions",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call to actions retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CallToActionDto>>>> getCallToActions(
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

    @Operation(summary = "Get call to action by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call to action retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Call to action not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<CallToActionDto>> getCallToActionById(
            @Parameter(description = "Call to action ID") @PathVariable String id
    );

    @Operation(summary = "Create call to action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Call to action created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<CallToActionDto>> createCallToAction(
            @Valid @RequestBody CallToActionCreateRequest request
    );

    @Operation(summary = "Update call to action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call to action updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Call to action not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<CallToActionDto>> updateCallToAction(
            @Parameter(description = "Call to action ID") @PathVariable String id,
            @Valid @RequestBody CallToActionUpdateRequest request
    );

    @Operation(summary = "Delete call to action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call to action deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Call to action not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteCallToAction(
            @Parameter(description = "Call to action ID") @PathVariable String id
    );
}
