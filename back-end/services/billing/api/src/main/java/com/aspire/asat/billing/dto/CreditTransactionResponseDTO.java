package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionResponseDTO {
    private String id;
    private String clientId;
    private String type;
    private Double amount;
    private String referenceType;
    private String referenceId;
    private String invoiceId;
    private String remarks;
    private TransactionStatus status;
    private boolean reversed;
    private Instant createdAt;
    private String description;
    private String initiatedBy;
}
