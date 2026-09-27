package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.*;
import com.aspire.asat.billing.dto.invoice.QuickRange;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.dto.payment.response.PaymentDetailsDTO;
import com.aspire.asat.billing.model.ClientInfo;
import com.aspire.asat.billing.model.Payment;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface PaymentService {
    ClientInfo saveClientInfo(ClientInfoRequestDTO clientInfoRequestDTO);

    Payment saveManualPayment(PaymentRequestDTO dto);

    PaymentResponseDTO saveOnlinePayment(PaymentRequestDTO dto);

    WebhookResponseDTO processStripeWebhook(String payload, String stripeSignature);

    WebhookResponseDTO processPaypalWebhook(String payload, HttpHeaders headers);

    CouponDiscountResponseDTO calculateBill(PaymentRequestDTO paymentRequestDTO, CouponCreateResponseDTO coupon);

    CouponDiscountResponseDTO calculateBill(PaymentRequestDTO paymentRequestDTO);

    List<PaymentHistoryItemDTO> getPaymentHistory(List<String> statuses, String method, String startDate, String endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search, int offset, int limit);

    long countPaymentHistory(List<String> statuses, String method, String startDate, String endDate, String clientId,
            String mspId, String countryId, RoleType roleType, String search);

    String generatePaymentHistoryExcel(List<String> statuses, String method, String startDate, String endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search);

    PaymentHistorySummaryDTO getPaymentHistorySummary(String startDate, String endDate, String clientId, String mspId,
            String countryId, RoleType roleType);

    byte[] generatePaymentHistoryCsv(List<String> statuses, String method, String startDate, String endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search);

    PaymentSummaryReportDTO getPaymentSummaryReport(String search, String clientAdminId, String mspId,
            List<String> statuses, String method, String startDate, String endDate, QuickRange quickRange);

    List<PaymentHistoryItemDTO> getPaymentSummaryReportList(String search, String clientAdminId, String mspId,
            List<String> statuses, String method, String startDate, String endDate, QuickRange quickRange,
            int offset, int limit);

    long countPaymentSummaryReport(String search, String clientAdminId, String mspId, List<String> statuses,
            String method, String startDate, String endDate, QuickRange quickRange);

    byte[] generatePaymentSummaryReportCsv(String search, String clientAdminId, String mspId, List<String> statuses,
            String method, String startDate, String endDate, QuickRange quickRange);

    PaymentDetailsDTO getPaymentDetails(String paymentId);

    PaymentResponseDTO payForUsedCredit(UsedCreditPaymentRequestDTO request);

    /**
     * Updates payment status and triggers related operations (invoice updates, notifications, etc.)
     * @param paymentId The payment ID
     * @param paymentStatus The new payment status ("paid" or "failed")
     * @param transactionId The transaction ID from the payment gateway
     */
    void updatePaymentStatus(String paymentId, String paymentStatus, String transactionId);

}
