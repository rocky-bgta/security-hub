package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.common.dto.invoice_logs.ActionRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.ActionResponseDTO;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.INVOICE_API + "/action", produces = "application/json")
@Tag(name = "Action Management", description = "Endpoints for managing action lookup data")
public interface ActionController {

    @Operation(summary = "Create a new action", description = "Creates a new action for use in comment logs")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Action created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid action data"),
            @ApiResponse(responseCode = "409", description = "Action with same name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ActionResponseDTO>> createAction(@Valid @RequestBody ActionRequestDTO requestDTO);

    @Operation(summary = "Get action by ID", description = "Retrieves an action by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Action retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Action not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<ActionResponseDTO>> getActionById(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Get all actions", description = "Retrieves all actions for dropdown selection")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<List<ActionResponseDTO>>> getAllActions();

    @Operation(summary = "Update an action", description = "Updates an existing action by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Action updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid action data"),
            @ApiResponse(responseCode = "404", description = "Action not found"),
            @ApiResponse(responseCode = "409", description = "Action with same name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<ActionResponseDTO>> updateAction(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody ActionRequestDTO requestDTO);

    @Operation(summary = "Delete an action", description = "Deletes an action by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Action deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Action not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteAction(@PathVariable("id") @NotBlank String id);
}

