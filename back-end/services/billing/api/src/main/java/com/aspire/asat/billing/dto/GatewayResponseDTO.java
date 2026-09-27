package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayResponseDTO {
    private String transactionId;
    private String checkoutUrl;
    private String status;
    private String message;
}
