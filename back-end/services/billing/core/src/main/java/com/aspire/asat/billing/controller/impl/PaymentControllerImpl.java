package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.PaymentController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.dto.payment.response.PaymentDetailsDTO;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.service.PaymentService;
import com.aspire.asat.common.enums.ActivityType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentControllerImpl implements PaymentController {

    private final PaymentService paymentService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Logged manual payment for invoice: #{#dto.invoiceId != null ? #dto.invoiceId : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<PaymentResponseDTO>> logManualPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        log.info("Logging manual payment for invoice: {}", dto.getInvoiceId());
        Payment savedPayment = paymentService.saveManualPayment(dto);
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(savedPayment.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Manual payment logged successfully", 201, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Logged online payment for invoice: #{#dto.invoiceId != null ? #dto.invoiceId : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<PaymentResponseDTO>> logOnlinePayment(@Valid @RequestBody PaymentRequestDTO dto) {
        log.info("Logging online payment for invoice: {}", dto.getInvoiceId());
        PaymentResponseDTO savedPayment = paymentService.saveOnlinePayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Online payment logged successfully", 201, savedPayment));
    }

    @Override
    public ResponseEntity<ApiResponseDto<WebhookResponseDTO>> handleStripePaymentWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature) {
        log.info("Received Stripe webhook request. Payload size: {} bytes, Signature present: {}", 
            payload != null ? payload.length() : 0, stripeSignature != null);
        
        WebhookResponseDTO responseDTO = paymentService.processStripeWebhook(payload, stripeSignature);
        
        log.info("Stripe webhook processed. Status: {}, Message: {}, TransactionId: {}, PaymentId: {}", 
            responseDTO.getStatus(), responseDTO.getMessage(), responseDTO.getTransactionId(), responseDTO.getPaymentId());
        
        return ResponseEntity.ok(new ApiResponseDto<>("Stripe webhook processed successfully", 200, responseDTO));
    }

    @Override
    public ResponseEntity<ApiResponseDto<WebhookResponseDTO>> handlePaypalPaymentWebhook(@RequestBody String payload, @RequestHeader HttpHeaders headers) {
        log.info("Processing PayPal payment webhook");
        WebhookResponseDTO responseDTO = paymentService.processPaypalWebhook(payload, headers);
        return ResponseEntity.ok(new ApiResponseDto<>("PayPal webhook processed successfully", 200, responseDTO));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PaymentHistoryItemDTO>>>> getPaymentHistory(
            String status, String method, String startDate, String endDate, String clientId, String mspId, String countryId, RoleType roleType, String search, int offset, int limit) {
        log.info("Getting payment history with filters - status: {}, method: {}, startDate: {}, endDate: {}, clientId: {}, mspId: {}, countryId: {}, roleType: {}, search: {}, offset: {}, limit: {}", 
                status, method, startDate, endDate, clientId, mspId, countryId, roleType, search, offset, limit);
        List<String> statuses = wrapStatus(status);
        List<PaymentHistoryItemDTO> result = paymentService.getPaymentHistory(statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search, offset, limit);
        long total = paymentService.countPaymentHistory(statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);
        AllResponseDto<List<PaymentHistoryItemDTO>> response = new AllResponseDto<>(offset, limit, total, result);
        return ResponseEntity.ok(new ApiResponseDto<>("Payment history retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> downloadPaymentHistoryExcel(String status, String method, String startDate, String endDate, String clientId, String mspId, String countryId, RoleType roleType) {
        log.info("Generating payment history Excel with filters - status: {}, method: {}, startDate: {}, endDate: {}, clientId: {}, mspId: {}, countryId: {}, roleType: {}", 
                status, method, startDate, endDate, clientId, mspId, countryId, roleType);
        String downloadLink = paymentService.generatePaymentHistoryExcel(wrapStatus(status), method, startDate, endDate, clientId, mspId, countryId, roleType, null);
        return ResponseEntity.ok(new ApiResponseDto<>("Download link generated successfully", 200, downloadLink));
    }

    @Override
    public ResponseEntity<ApiResponseDto<PaymentHistorySummaryDTO>> getPaymentHistorySummary(
            String startDate, String endDate, String clientId, String mspId, String countryId, RoleType roleType) {
        log.info("Getting payment history summary - startDate: {}, endDate: {}, clientId: {}, mspId: {}, countryId: {}, roleType: {}", 
                startDate, endDate, clientId, mspId, countryId, roleType);
        PaymentHistorySummaryDTO summary = paymentService.getPaymentHistorySummary(startDate, endDate, clientId, mspId, countryId, roleType);
        return ResponseEntity.ok(new ApiResponseDto<>("Payment history summary retrieved successfully", 200, summary));
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadPaymentHistoryCsv(
            String status, String method, String startDate, String endDate, String clientId, String mspId, String countryId, RoleType roleType) {
        log.info("Generating payment history CSV with filters - status: {}, method: {}, startDate: {}, endDate: {}, clientId: {}, mspId: {}, countryId: {}, roleType: {}", 
                status, method, startDate, endDate, clientId, mspId, countryId, roleType);
        byte[] csvData = paymentService.generatePaymentHistoryCsv(wrapStatus(status), method, startDate, endDate, clientId, mspId, countryId, roleType, null);
        
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(csvData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payment-history-" + System.currentTimeMillis() + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(resource);
    }

    @Override
    public ResponseEntity<ApiResponseDto<PaymentSummaryReportDTO>> getPaymentSummaryReport(
            String search, String clientAdminId, String mspId, List<String> statuses, String method,
            String startDate, String endDate, QuickRange quickRange) {
        log.info("Getting payment summary report - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, method: {}, startDate: {}, endDate: {}, quickRange: {}",
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange);
        PaymentSummaryReportDTO summary = paymentService.getPaymentSummaryReport(
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange);
        return ResponseEntity.ok(new ApiResponseDto<>("Payment summary report retrieved successfully", 200, summary));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PaymentHistoryItemDTO>>>> getPaymentSummaryReportList(
            String search, String clientAdminId, String mspId, List<String> statuses, String method,
            String startDate, String endDate, QuickRange quickRange, int offset, int limit) {
        log.info("Getting payment summary report list - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, method: {}, startDate: {}, endDate: {}, quickRange: {}, offset: {}, limit: {}",
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange, offset, limit);

        int effectiveLimit = Math.min(limit, 100);
        List<PaymentHistoryItemDTO> items = paymentService.getPaymentSummaryReportList(
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange, offset, effectiveLimit);
        long total = paymentService.countPaymentSummaryReport(
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange);

        AllResponseDto<List<PaymentHistoryItemDTO>> response = new AllResponseDto<>(offset, effectiveLimit, total, items);
        return ResponseEntity.ok(new ApiResponseDto<>("Payment summary report list retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadPaymentSummaryReportCsv(
            String search, String clientAdminId, String mspId, List<String> statuses, String method,
            String startDate, String endDate, QuickRange quickRange) {
        log.info("Exporting payment summary report CSV - search: {}, clientAdminId: {}, mspId: {}, statuses: {}, method: {}, startDate: {}, endDate: {}, quickRange: {}",
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange);

        byte[] csvData = paymentService.generatePaymentSummaryReportCsv(
                search, clientAdminId, mspId, statuses, method, startDate, endDate, quickRange);

        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(csvData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=payment-summary-report-" + System.currentTimeMillis() + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(resource);
    }

    private static List<String> wrapStatus(String status) {
        return status == null ? null : List.of(status);
    }

    @Override
    public ResponseEntity<ApiResponseDto<PaymentDetailsDTO>> getPaymentDetails(String paymentId) {
        log.info("Getting payment details for payment ID: {}", paymentId);
        PaymentDetailsDTO dto = paymentService.getPaymentDetails(paymentId);
        return ResponseEntity.ok(new ApiResponseDto<>("Payment details retrieved successfully", 200, dto));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Payment for used credit transaction: #{#requestDTO.creditTransactionId != null ? #requestDTO.creditTransactionId : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<PaymentResponseDTO>> payForUsedCredit(
            @Valid @RequestBody UsedCreditPaymentRequestDTO requestDTO) {
        log.info("Processing payment for used credit transaction: {}", requestDTO.getCreditTransactionId());
        PaymentResponseDTO response = paymentService.payForUsedCredit(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Used credit payment initiated successfully", 201, response));
    }

}
