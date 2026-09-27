package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.dto.TransactionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.CREDIT_API, produces = "application/json")
@Tag(name = "Credit Management", description = "Endpoints for Credit Management")
public interface CreditController {

    @Operation(summary = "Create new credit", description = "Allows admins to add credits to a client account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Credit created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(WebApiUrlConstants.CREATE)
    ResponseEntity<ApiResponseDto<CreditResponseDTO>> createCredit(@Valid @RequestBody CreditCreateRequestDTO requestDTO);

    @Operation(summary = "Update credit", description = "Updates a credit entry by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit updated successfully"),
            @ApiResponse(responseCode = "404", description = "Credit not found")
    })
    @PutMapping("/update/{creditId}")
    ResponseEntity<ApiResponseDto<CreditResponseDTO>> updateCredit(
            @PathVariable("creditId") @NotBlank String creditId,
            @Valid @RequestBody CreditUpdateRequestDTO requestDTO);

    @Operation(summary = "Get credit by ID", description = "Fetches credit details by credit ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Credit not found")
    })
    @GetMapping("/id/{creditId}")
    ResponseEntity<ApiResponseDto<CreditResponseDTO>> getCreditById(@PathVariable("creditId") @NotBlank String creditId);

    @Operation(summary = "List credits by client ID", description = "Returns credit information of a client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credits listed successfully"),
            @ApiResponse(responseCode = "404", description = "Credits not found for client")
    })
    @GetMapping("/client/{clientId}")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditResponseDTO>>>> getCreditsByClientId(@PathVariable("clientId") @NotBlank String clientId);

    @Operation(summary = "Deactivate credit", description = "Marks a credit entry as inactive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Credit not found")
    })
    @PutMapping("/switch-mode/{creditId}")
    ResponseEntity<ApiResponseDto<CreditResponseDTO>> deactivateCredit(@PathVariable("creditId") @NotBlank String creditId);


    @Operation(summary = "Log a credit transaction", description = "Logs a credit transaction for any type including transfer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction logged successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or insufficient details")
    })
    @PostMapping("/transaction/log")
    ResponseEntity<ApiResponseDto<CreditTransactionResponseDTO>> logCreditTransaction(
            @Valid @RequestBody CreditTransactionCreateDTO transactionDTO);

    @Operation(summary = "Get credit transaction by ID", description = "Retrieves a credit transaction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/transaction/id/{transactionId}")
    ResponseEntity<ApiResponseDto<CreditTransactionResponseDTO>> getCreditTransactionById(
            @PathVariable("transactionId") @NotBlank String transactionId);

    @Operation(summary = "Get transactions by credit ID", description = "Returns all transactions related to a credit entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions fetched successfully")
    })
    @GetMapping("/transaction/credit/{creditId}")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditTransactionResponseDTO>>>> getTransactionsByCreditId(
            @PathVariable("creditId") @NotBlank String creditId);

    @Operation(summary = "Get transactions by client ID", description = "Returns all credit transactions for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions fetched successfully")
    })
    @GetMapping("/transaction/client/{clientId}")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditTransactionResponseDTO>>>> getTransactionsByClientId(
            @PathVariable("clientId") @NotBlank String clientId);

    @PostMapping("/transfer")
    ResponseEntity<ApiResponseDto<CreditTransferResponseDTO>> transferCredit(
            @Valid @RequestBody CreditTransferRequestDTO transferRequestDTO);

    @Operation(summary = "Deposit credit", description = "Adds credit to an existing credit account")
    @PostMapping("/deposit")
    ResponseEntity<ApiResponseDto<CreditResponseDTO>> depositCredit(@Valid @RequestBody CreditOperationRequestDTO request);

    @Operation(summary = "Withdraw credit", description = "Withdraws credit from an existing credit account")
    @PostMapping("/withdraw")
    ResponseEntity<ApiResponseDto<CreditResponseDTO>> withdrawCredit(@Valid @RequestBody CreditOperationRequestDTO request);

    @Operation(
            summary = "Get credit usage summary by status",
            description = "Returns count and total amount of credit used by status for a client, with optional filters like invoiceId, status, type, and pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    })
    @GetMapping("/transaction/usage-list/{clientId}")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditTransactionResponseDTO>>>> getCreditUsageSummaryByClientId(
            @PathVariable("clientId") @NotBlank String clientId,
            @RequestParam(value = "invoiceId", required = false) String invoiceId,
            @RequestParam(value = "status", required = false) TransactionStatus status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    @Operation(
            summary = "Get overall credit usage summary totals",
            description = "Returns total used, paid, and due credits for a given client"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    })
    @GetMapping("/transaction/usage-summary/{clientId}")
    ResponseEntity<ApiResponseDto<CreditUsageSummaryDTO>> getCreditUsageTotalsByClientId(
            @PathVariable("clientId") @NotBlank String clientId,
            @RequestParam(value = "type", required = false) String type
    );



}
