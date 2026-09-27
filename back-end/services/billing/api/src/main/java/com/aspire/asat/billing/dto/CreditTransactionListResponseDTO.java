package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Credit Transaction List Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransactionListResponseDTO {
    private List<CreditTransactionResponseDTO> transactions;
    private int totalCount;
}
