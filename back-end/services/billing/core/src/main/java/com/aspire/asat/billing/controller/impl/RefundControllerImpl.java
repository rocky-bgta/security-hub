package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.RefundController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.refund.RefundRequestDTO;
import com.aspire.asat.billing.dto.refund.RefundResponseDTO;
import com.aspire.asat.billing.dto.refund.RefundStatus;
import com.aspire.asat.billing.service.RefundService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RefundControllerImpl implements RefundController {

    private final RefundService refundService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created refund for payment: #{#requestDTO.paymentId != null ? #requestDTO.paymentId : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<RefundResponseDTO>> createRefund(RefundRequestDTO requestDTO, String userId) {
        log.info("Creating refund for payment: {}, by user: {}", requestDTO.getPaymentId(), userId);
        
        RefundResponseDTO refund = refundService.createRefund(requestDTO, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
                "Refund created successfully",
                HttpStatus.CREATED.value(),
                refund
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<RefundResponseDTO>> getRefundById(String id) {
        log.info("Getting refund by ID: {}", id);
        
        RefundResponseDTO refund = refundService.getRefundById(id);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Refund retrieved successfully",
                HttpStatus.OK.value(),
                refund
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<RefundResponseDTO>>>> getRefunds(
            String clientId, String mspId, String countryId, RefundStatus status,
            String startDate, String endDate, int offset, int limit) {
        log.info("Getting refunds - clientId: {}, status: {}, offset: {}, limit: {}", clientId, status, offset, limit);
        
        List<RefundResponseDTO> refunds = refundService.getRefunds(clientId, mspId, countryId, status, 
                startDate, endDate, offset, limit);
        long total = refundService.countRefunds(clientId, mspId, countryId, status, startDate, endDate);
        
        AllResponseDto<List<RefundResponseDTO>> response = new AllResponseDto<>(offset, limit, total, refunds);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Refunds retrieved successfully",
                HttpStatus.OK.value(),
                response
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<RefundResponseDTO>>> getRefundsByPaymentId(String paymentId) {
        log.info("Getting refunds by payment ID: {}", paymentId);
        
        List<RefundResponseDTO> refunds = refundService.getRefundsByPaymentId(paymentId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Refunds retrieved successfully",
                HttpStatus.OK.value(),
                refunds
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<RefundResponseDTO>>> getRefundsByInvoiceId(String invoiceId) {
        log.info("Getting refunds by invoice ID: {}", invoiceId);
        
        List<RefundResponseDTO> refunds = refundService.getRefundsByInvoiceId(invoiceId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Refunds retrieved successfully",
                HttpStatus.OK.value(),
                refunds
        ));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Processed refund: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#status != null ? #status.toString() : #id}"
    )
    public ResponseEntity<ApiResponseDto<RefundResponseDTO>> processRefund(String id, RefundStatus status, String transactionId) {
        log.info("Processing refund: {} with status: {}", id, status);
        
        RefundResponseDTO refund = refundService.processRefund(id, status, transactionId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Refund processed successfully",
                HttpStatus.OK.value(),
                refund
        ));
    }
}

