package com.aspire.asat.billing.dto;

import com.aspire.asat.billing.dto.TransactionStatus;
import lombok.Data;

@Data
public class CreditTransactionUpdateRequestDTO {
    private String transactionId;      // required
    private TransactionStatus status;  // PENDING, PAID, etc.
    private String referenceId;        // Stripe session ID, PayPal ID, or bank ref
    private String remarks;            // Optional: update reason
}
