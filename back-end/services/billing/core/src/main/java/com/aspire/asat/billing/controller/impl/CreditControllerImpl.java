package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.CreditController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.dto.TransactionStatus;
import com.aspire.asat.billing.service.CreditService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CreditControllerImpl implements CreditController {

    private final CreditService creditService;

    @Override
    @LogActivity(
            activityType = ActivityType.CREDIT_ENABLED,
            description = "Created credit for client: #{#dto.clientId != null ? #dto.clientId : 'N/A'}",
            clientAdminIdExpression = "#{#dto.clientId}"
    )
    public ResponseEntity<ApiResponseDto<CreditResponseDTO>> createCredit(CreditCreateRequestDTO dto) {
        log.info("Creating credit for client: {}", dto.getClientId());
        CreditResponseDTO response = creditService.createCredit(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Credit created successfully", 201, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.CREDIT_UPDATED,
            description = "Updated credit: #{#creditId}",
            oldValueExpression = "#{#creditId}",
            newValueExpression = "#{#dto.creditAmount != null ? #dto.creditAmount.toString() : #creditId}"
    )
    public ResponseEntity<ApiResponseDto<CreditResponseDTO>> updateCredit(String creditId, CreditUpdateRequestDTO dto) {
        log.info("Updating credit: {}", creditId);
        CreditResponseDTO response = creditService.updateCredit(creditId, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditResponseDTO>> getCreditById(String creditId) {
        log.info("Getting credit by ID: {}", creditId);
        CreditResponseDTO response = creditService.getCreditById(creditId);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditResponseDTO>>>> getCreditsByClientId(String clientId) {
        log.info("Getting credits for client: {}", clientId);
        List<CreditResponseDTO> response = creditService.getCreditsByClientId(clientId);
        long total = response.size();
        AllResponseDto<List<CreditResponseDTO>> allResponse = new AllResponseDto<>(0, response.size(), total, response);
        return ResponseEntity.ok(new ApiResponseDto<>("Credits retrieved successfully", 200, allResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditResponseDTO>> deactivateCredit(String creditId) {
        log.info("Deactivating credit: {}", creditId);
        CreditResponseDTO response = creditService.deactivateCredit(creditId);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit deactivated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditTransactionResponseDTO>> logCreditTransaction(CreditTransactionCreateDTO dto) {
        log.info("Logging credit transaction for client: {}", dto.getClientId());
        CreditTransactionResponseDTO response = creditService.logCreditTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Credit transaction logged successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditTransactionResponseDTO>> getCreditTransactionById(String transactionId) {
        log.info("Getting credit transaction by ID: {}", transactionId);
        CreditTransactionResponseDTO response = creditService.getCreditTransactionById(transactionId);
        return ResponseEntity.ok(new ApiResponseDto<>("Transaction retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditTransactionResponseDTO>>>> getTransactionsByCreditId(String creditId) {
        log.info("Getting transactions for credit: {}", creditId);
        List<CreditTransactionResponseDTO> response = creditService.getTransactionsByCreditId(creditId);
        long total = response.size();
        AllResponseDto<List<CreditTransactionResponseDTO>> allResponse = new AllResponseDto<>(0, response.size(), total, response);
        return ResponseEntity.ok(new ApiResponseDto<>("Transactions retrieved successfully", 200, allResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditTransactionResponseDTO>>>> getTransactionsByClientId(String clientId) {
        log.info("Getting transactions for client: {}", clientId);
        List<CreditTransactionResponseDTO> response = creditService.getTransactionsByClientId(clientId);
        long total = response.size();
        AllResponseDto<List<CreditTransactionResponseDTO>> allResponse = new AllResponseDto<>(0, response.size(), total, response);
        return ResponseEntity.ok(new ApiResponseDto<>("Transactions retrieved successfully", 200, allResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditTransferResponseDTO>> transferCredit(CreditTransferRequestDTO transferRequestDTO) {
        log.info("Transferring credit from client: {} to client: {}", transferRequestDTO.getFromClientId(), transferRequestDTO.getToClientId());
        CreditTransferResponseDTO result = creditService.transferCredit(transferRequestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit transferred successfully", 200, result));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditResponseDTO>> depositCredit(CreditOperationRequestDTO request) {
        log.info("Depositing credit for client: {}", request.getClientId());
        CreditResponseDTO response = creditService.depositCredit(request);
        return ResponseEntity.ok(new ApiResponseDto<>("Deposit successful", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditResponseDTO>> withdrawCredit(CreditOperationRequestDTO request) {
        log.info("Withdrawing credit for client: {}", request.getClientId());
        CreditResponseDTO response = creditService.withdrawCredit(request);
        return ResponseEntity.ok(new ApiResponseDto<>("Withdrawal successful", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CreditTransactionResponseDTO>>>> getCreditUsageSummaryByClientId(
            String clientId,
            String invoiceId,
            TransactionStatus status,
            String type,
            int offset,
            int limit) {
        log.info("Getting credit usage summary for client: {} with filters - invoiceId: {}, status: {}, type: {}, offset: {}, limit: {}", 
                clientId, invoiceId, status, type, offset, limit);
        List<CreditTransactionResponseDTO> response = creditService.getCreditUsageSummaryByClientId(
                clientId, invoiceId, status, type, offset, limit);
        long total = creditService.countCreditUsageSummaryByClientId(clientId, invoiceId, status, type);
        AllResponseDto<List<CreditTransactionResponseDTO>> allResponse = new AllResponseDto<>(offset, limit, total, response);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit usage summary retrieved successfully", 200, allResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CreditUsageSummaryDTO>> getCreditUsageTotalsByClientId(
            String clientId,
            String type) {
        log.info("Getting credit usage totals for client: {}, type: {}", clientId, type);
        CreditUsageSummaryDTO summary = creditService.getCreditUsageTotalsByClientId(clientId, type);
        return ResponseEntity.ok(new ApiResponseDto<>("Credit usage summary retrieved successfully", 200, summary));
    }

}
