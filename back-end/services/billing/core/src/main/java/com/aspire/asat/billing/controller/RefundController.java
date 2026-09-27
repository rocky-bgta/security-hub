package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.refund.RefundRequestDTO;
import com.aspire.asat.billing.dto.refund.RefundResponseDTO;
import com.aspire.asat.billing.dto.refund.RefundStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.REFUND_API, produces = "application/json")
@Tag(name = "Refund Management", description = "Endpoints for managing refunds")
public interface RefundController {

    @Operation(summary = "Create a refund", description = "Creates a new refund for a payment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Refund created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Payment or invoice not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    ResponseEntity<ApiResponseDto<RefundResponseDTO>> createRefund(
            @Valid @RequestBody RefundRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    );

    @Operation(summary = "Get refund by ID", description = "Returns a refund by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Refund not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<RefundResponseDTO>> getRefundById(@PathVariable String id);

    @Operation(summary = "Get refunds list", description = "Returns a paginated list of refunds with filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<RefundResponseDTO>>>> getRefunds(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId,
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Offset must be >= 0") int offset,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be >= 1") int limit
    );

    @Operation(summary = "Get refunds by payment ID", description = "Returns all refunds for a specific payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-payment/{paymentId}")
    ResponseEntity<ApiResponseDto<List<RefundResponseDTO>>> getRefundsByPaymentId(@PathVariable String paymentId);

    @Operation(summary = "Get refunds by invoice ID", description = "Returns all refunds for a specific invoice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-invoice/{invoiceId}")
    ResponseEntity<ApiResponseDto<List<RefundResponseDTO>>> getRefundsByInvoiceId(@PathVariable String invoiceId);

    @Operation(summary = "Process a refund", description = "Updates refund status to PROCESSED or FAILED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
            @ApiResponse(responseCode = "400", description = "Refund not in PENDING status"),
            @ApiResponse(responseCode = "404", description = "Refund not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/process")
    ResponseEntity<ApiResponseDto<RefundResponseDTO>> processRefund(
            @PathVariable String id,
            @RequestParam RefundStatus status,
            @RequestParam(required = false) String transactionId
    );
}

