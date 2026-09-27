package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.creditAllocation.CreateCreditAllocationReasonRequestDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonDropdownDTO;
import com.aspire.asat.registration.data.creditAllocation.CreditAllocationReasonResponseDTO;
import com.aspire.asat.registration.data.creditAllocation.UpdateCreditAllocationReasonRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Credit Allocation Reason", description = "Manage reasons for credit allocation")
@RequestMapping(value = "/api/credit-allocation-reason", produces = "application/json")
public interface CreditAllocationReasonController {

    @Operation(summary = "Create a new credit allocation reason")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credit allocation reason created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<CreditAllocationReasonResponseDTO>> createReason(
            @Valid @RequestBody CreateCreditAllocationReasonRequestDTO dto
    );

    @Operation(summary = "Get all credit allocation reasons with pagination and filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credit allocation reasons retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditAllocationReasonResponseDTO>>>> getAllReasons(
            @RequestParam(value = "search", required = false)
            @Parameter(description = "Search by reason name or description (partial match)") String search,

            @RequestParam(value = "isActive", required = false)
            @Parameter(description = "Filter by active status (true for active, false for inactive)") Boolean isActive,

            @RequestParam(value = "offset", defaultValue = "0")
            @Parameter(description = "Pagination offset") int offset,

            @RequestParam(value = "limit", defaultValue = "10")
            @Parameter(description = "Number of items to return") int limit
    );

    @Operation(summary = "Get credit allocation reason by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credit allocation reason fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Credit allocation reason not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<CreditAllocationReasonResponseDTO>> getReasonById(
            @PathVariable("id") @Parameter(description = "Credit allocation reason ID") String id
    );

    @Operation(summary = "Update an existing credit allocation reason")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credit allocation reason updated successfully"),
            @ApiResponse(responseCode = "404", description = "Credit allocation reason not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<CreditAllocationReasonResponseDTO>> updateReason(
            @PathVariable("id") @Parameter(description = "Credit allocation reason ID") String id,
            @Valid @RequestBody UpdateCreditAllocationReasonRequestDTO dto
    );

    @Operation(summary = "Delete a credit allocation reason")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credit allocation reason deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Credit allocation reason not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteReason(
            @PathVariable("id") @Parameter(description = "Credit allocation reason ID") String id
    );

    @Operation(summary = "Get all active credit allocation reasons for dropdown")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active credit allocation reasons retrieved successfully")
    })
    @GetMapping("/active")
    ResponseEntity<ApiResponseDto<List<CreditAllocationReasonDropdownDTO>>> getAllActiveReasons();
}

