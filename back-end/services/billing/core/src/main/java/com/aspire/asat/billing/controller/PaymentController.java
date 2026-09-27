package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.dto.payment.response.PaymentDetailsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping(value = WebApiUrlConstants.PAYMENT_API, produces = "application/json")
public interface PaymentController {

    @Operation(summary = "Log a manual payment", description = "Logs a manual payment performed via bank transfer, cheque, or other offline methods. Supports optional coupon application and updates usage accordingly.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Manual payment logged successfully"), @ApiResponse(responseCode = "400", description = "Bad request: Invalid data or missing required fields"), @ApiResponse(responseCode = "404", description = "Invoice, Client, or Coupon not found"), @ApiResponse(responseCode = "409", description = "Conflict: Coupon already used or invalid"), @ApiResponse(responseCode = "422", description = "Unprocessable Entity: Coupon conditions not met"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping(WebApiUrlConstants.PAYMENT_ENTRY)
    ResponseEntity<ApiResponseDto<PaymentResponseDTO>> logManualPayment(@Valid @RequestBody PaymentRequestDTO dto);

    @Operation(summary = "Log an online payment", description = "Logs an online payment performed via Stripe, PayPal, or other online methods. Supports optional coupon application and updates usage accordingly.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Online payment logged successfully"), @ApiResponse(responseCode = "400", description = "Bad request: Invalid data or missing required fields"), @ApiResponse(responseCode = "404", description = "Invoice, Client, or Coupon not found"), @ApiResponse(responseCode = "409", description = "Conflict: Coupon already used or invalid"), @ApiResponse(responseCode = "422", description = "Unprocessable Entity: Coupon conditions not met"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping(WebApiUrlConstants.ONLINE_PAYMENT_ENTRY)
    ResponseEntity<ApiResponseDto<PaymentResponseDTO>> logOnlinePayment(@Valid @RequestBody PaymentRequestDTO dto);


    @Operation(summary = "Stripe payment webhook", description = "Receives and processes Stripe webhook events for payment confirmation (e.g., checkout.session.completed).")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Stripe webhook processed successfully"), @ApiResponse(responseCode = "400", description = "Invalid Stripe webhook signature or bad request"), @ApiResponse(responseCode = "500", description = "Internal server error while processing webhook")})
    @PostMapping(WebApiUrlConstants.STRIPE_API + WebApiUrlConstants.WEBHOOK + WebApiUrlConstants.PAYMENT)
    ResponseEntity<ApiResponseDto<WebhookResponseDTO>> handleStripePaymentWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature);

    @Operation(summary = "PayPal payment webhook", description = "Receives and processes PayPal webhook events such as PAYMENT.CAPTURE.COMPLETED for order confirmation.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "PayPal webhook processed successfully"), @ApiResponse(responseCode = "400", description = "Bad request: Invalid or missing required headers"), @ApiResponse(responseCode = "500", description = "Internal server error while processing webhook")})
    @PostMapping(WebApiUrlConstants.PAYPAL + WebApiUrlConstants.WEBHOOK + WebApiUrlConstants.PAYMENT)
    ResponseEntity<ApiResponseDto<WebhookResponseDTO>> handlePaypalPaymentWebhook(@RequestBody String payload, @RequestHeader HttpHeaders headers);


    @Operation(summary = "Get payment history", description = "Fetch a paginated list of payment history with filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filters or pagination"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/history")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PaymentHistoryItemDTO>>>> getPaymentHistory(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "startDate", required = false) String startDate,  // format: yyyy-MM-dd
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "countryId", required = false) String countryId,
            @RequestParam(value = "roleType", required = false) RoleType roleType,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int limit
    );

    @GetMapping("/history/download")
    @Operation(summary = "Download payment history as Excel", description = "Generates an Excel sheet of all filtered payment history and returns a public download link.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download link generated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate Excel or upload")
    })
    ResponseEntity<ApiResponseDto<String>> downloadPaymentHistoryExcel(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "countryId", required = false) String countryId,
            @RequestParam(value = "roleType", required = false) RoleType roleType
    );

    @GetMapping("/history/summary")
    @Operation(summary = "Get payment history summary statistics", description = "Returns summary statistics including total payments, outstanding amounts, and invoice counts with month-over-month changes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary statistics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<PaymentHistorySummaryDTO>> getPaymentHistorySummary(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "countryId", required = false) String countryId,
            @RequestParam(value = "roleType", required = false) RoleType roleType
    );

    @GetMapping("/history/export/csv")
    @Operation(summary = "Export payment history as CSV", description = "Generates a CSV file of all filtered payment history and returns it as a download")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    ResponseEntity<org.springframework.core.io.Resource> downloadPaymentHistoryCsv(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "countryId", required = false) String countryId,
            @RequestParam(value = "roleType", required = false) RoleType roleType
    );

    @Operation(
            summary = "Get payment summary report counts",
            description = "Returns payment status counts and amount sums for the summary report. "
                    + "Supports optional filters: search, clientAdminId, mspId, status, method, startDate, endDate, quickRange. "
                    + "When quickRange is provided it takes precedence over startDate/endDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary counts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary-report/summary")
    ResponseEntity<ApiResponseDto<PaymentSummaryReportDTO>> getPaymentSummaryReport(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "quickRange", required = false) QuickRange quickRange
    );

    @Operation(
            summary = "Get payment summary report detail list",
            description = "Returns a paginated list of payments for the summary report. "
                    + "Each item is a PaymentHistoryItemDTO enriched with invoice client/MSP names. "
                    + "Supports optional filters: search, clientAdminId, mspId, status, method, startDate, endDate, quickRange. "
                    + "When quickRange is provided it takes precedence over startDate/endDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment detail list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary-report/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PaymentHistoryItemDTO>>>> getPaymentSummaryReportList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "quickRange", required = false) QuickRange quickRange,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Offset must be >= 0") int offset,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Limit must be >= 1") int limit
    );

    @Operation(
            summary = "Export payment summary report as CSV",
            description = "Generates a CSV of filtered payments for the summary report and returns it as a download. "
                    + "Supports the same optional filters as the summary and list endpoints. "
                    + "When quickRange is provided it takes precedence over startDate/endDate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/summary-report/export/csv")
    ResponseEntity<org.springframework.core.io.Resource> downloadPaymentSummaryReportCsv(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "quickRange", required = false) QuickRange quickRange
    );


    @GetMapping("/details/{paymentId}")
    @Operation(summary = "Get payment details by ID", description = "Returns detailed information about a payment including invoice data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment details fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    ResponseEntity<ApiResponseDto<PaymentDetailsDTO>> getPaymentDetails(@PathVariable String paymentId);


    @Operation(
            summary = "Pay for a used credit transaction",
            description = "Allows paying an existing credit transaction using manual, Stripe, or PayPal. Updates the transaction status to PENDING and logs reference details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Used credit payment initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Credit transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/credit/pay")
    ResponseEntity<ApiResponseDto<PaymentResponseDTO>> payForUsedCredit(
            @Valid @RequestBody UsedCreditPaymentRequestDTO requestDTO
    );



}
