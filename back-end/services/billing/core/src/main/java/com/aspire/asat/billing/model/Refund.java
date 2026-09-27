package com.aspire.asat.billing.model;

import com.aspire.asat.billing.dto.refund.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Model representing a refund transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refunds")
public class Refund {

    @Id
    private String id;

    private String paymentId;       // Reference to the original payment
    private String invoiceId;       // Reference to the associated invoice
    private String clientId;        // Client receiving the refund
    private String mspAdminId;      // MSP Admin ID for filtering
    private String countryId;       // Country ID for filtering

    private Double refundAmount;    // Amount being refunded
    private String currency;        // Currency of the refund

    private String reason;          // Reason for the refund
    private RefundStatus status;    // PENDING, PROCESSED, FAILED

    private Instant refundedAt;     // When the refund was processed
    private Instant createdAt;      // When the refund was initiated
    private String processedBy;     // Admin who processed the refund

    private String transactionId;   // Gateway transaction ID if applicable
    private String notes;           // Additional notes
}

