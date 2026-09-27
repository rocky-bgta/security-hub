package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.CommentLogResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.INVOICE_API + "/comment-log", produces = "application/json")
@Tag(name = "Invoice Comment Log Management", description = "Endpoints for managing invoice comment logs")
public interface CommentLogController {

    @Operation(summary = "Create a new comment log", description = "Creates a new comment log for an invoice with user context")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment log created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment log data"),
            @ApiResponse(responseCode = "404", description = "Invoice, Action, or NextStep not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<CommentLogResponseDTO>> createCommentLog(@Valid @RequestBody CommentLogRequestDTO requestDTO);

    @Operation(summary = "Get comment log by ID", description = "Retrieves a comment log by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment log retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Comment log not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<CommentLogResponseDTO>> getCommentLogById(@PathVariable("id") @NotBlank String id);

    @Operation(summary = "Get comment logs by invoice ID", description = "Retrieves all comment logs for a specific invoice, ordered by date descending")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment logs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/invoice/{invoiceId}")
    ResponseEntity<ApiResponseDto<List<CommentLogResponseDTO>>> getCommentLogsByInvoiceId(@PathVariable("invoiceId") @NotBlank String invoiceId);

    @Operation(summary = "Update a comment log", description = "Updates an existing comment log by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment log updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment log data"),
            @ApiResponse(responseCode = "404", description = "Comment log, Invoice, Action, or NextStep not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<CommentLogResponseDTO>> updateCommentLog(
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody CommentLogRequestDTO requestDTO);

    @Operation(summary = "Delete a comment log", description = "Deletes a comment log by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment log deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment log not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteCommentLog(@PathVariable("id") @NotBlank String id);
}

