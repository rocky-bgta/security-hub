package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.common.dto.invoice_logs.NextStepRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.NextStepResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.INVOICE_API + "/next-step", produces = "application/json")
@Tag(name = "Next Step Management", description = "Endpoints for managing next step lookup data")
public interface NextStepController {

    @Operation(summary = "Create a new next step", description = "Creates a new next step for use in comment logs")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Next step created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid next step data"),
            @ApiResponse(responseCode = "409", description = "Next step with same name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<NextStepResponseDTO>> createNextStep(@Valid @RequestBody NextStepRequestDTO requestDTO);

    @Operation(summary = "Get next step by ID", description = "Retrieves a next step by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next step retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Next step not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<NextStepResponseDTO>> getNextStepById(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Get all next steps", description = "Retrieves all next steps for dropdown selection")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next steps retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<List<NextStepResponseDTO>>> getAllNextSteps();

    @Operation(summary = "Update a next step", description = "Updates an existing next step by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next step updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid next step data"),
            @ApiResponse(responseCode = "404", description = "Next step not found"),
            @ApiResponse(responseCode = "409", description = "Next step with same name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<NextStepResponseDTO>> updateNextStep(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody NextStepRequestDTO requestDTO);

    @Operation(summary = "Delete a next step", description = "Deletes a next step by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next step deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Next step not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteNextStep(@PathVariable("id") @NotBlank String id);
}

