package com.aspire.asat.billing.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentGatewayRequestDTO {

    private String paymentId;   // Payment._id
    private String invoiceId;   // Linked invoice
    private String clientId;    // Client making the payment
    private String currency;    // ISO Currency (e.g., BDT, USD)
    private Double amount;      // Amount to pay via this gateway
}
