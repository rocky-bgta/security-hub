package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookResponseDTO {
    private String status;  // success / ignored / error
    private String message; // human readable
    private String transactionId;
    private String paymentId;
}
