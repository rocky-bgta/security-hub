package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransferResponseDTO {
    private String fromClientId;
    private String toClientId;
    private Double amountTransferred;
    private String status;
    private String message;
}
