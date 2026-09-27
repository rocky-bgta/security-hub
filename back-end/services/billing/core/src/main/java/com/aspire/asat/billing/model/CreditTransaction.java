package com.aspire.asat.billing.model;

import com.aspire.asat.billing.dto.TransactionStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "credit_transactions")
public class CreditTransaction {

    @Id
    private String id;

    private String clientId;         // The client performing the transaction
    private String type;             // DEPOSIT, USAGE, TRANSFER_IN, TRANSFER_OUT, REVERSAL
    private Double amount;           // Amount added/used
    private String referenceType;    // PAYMENT, REFUND, MANUAL_ADJUSTMENT, TRANSFER
    private String referenceId;      // paymentId, adminId, etc.
    private String remarks;          // Optional notes

    private boolean reversed = false;
    private Instant createdAt = Instant.now();

    private TransactionStatus status;
    private String invoiceId;

}
