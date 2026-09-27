package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.refund.RefundRequestDTO;
import com.aspire.asat.billing.dto.refund.RefundResponseDTO;
import com.aspire.asat.billing.dto.refund.RefundStatus;

import java.util.List;

/**
 * Service interface for refund operations.
 */
public interface RefundService {

    /**
     * Create a new refund.
     */
    RefundResponseDTO createRefund(RefundRequestDTO requestDTO, String processedBy);

    /**
     * Get refund by ID.
     */
    RefundResponseDTO getRefundById(String id);

    /**
     * Get refunds with filters.
     */
    List<RefundResponseDTO> getRefunds(String clientId, String mspId, String countryId, 
                                        RefundStatus status, String startDate, String endDate,
                                        int offset, int limit);

    /**
     * Count refunds with filters.
     */
    long countRefunds(String clientId, String mspId, String countryId, 
                      RefundStatus status, String startDate, String endDate);

    /**
     * Get refunds by payment ID.
     */
    List<RefundResponseDTO> getRefundsByPaymentId(String paymentId);

    /**
     * Get refunds by invoice ID.
     */
    List<RefundResponseDTO> getRefundsByInvoiceId(String invoiceId);

    /**
     * Process a pending refund (mark as PROCESSED or FAILED).
     */
    RefundResponseDTO processRefund(String refundId, RefundStatus status, String transactionId);
}

