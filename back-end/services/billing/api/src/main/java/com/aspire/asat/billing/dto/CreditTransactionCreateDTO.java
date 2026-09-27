package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransactionCreateDTO {
    private String clientId;           // Who initiated
    private String receiverClientId;  // For TRANSFER
    private Double amount;
    private String type;              // DEPOSIT, USAGE, TRANSFER, REVERSAL
    private String description;
    private String referenceId;
    private String referenceType;
    private String initiatedBy;
    private TransactionStatus status;
}
