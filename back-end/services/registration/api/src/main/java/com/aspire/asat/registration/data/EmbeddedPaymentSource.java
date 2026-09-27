package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedPaymentSource {

    private String method;         // "CREDIT", "STRIPE", "PAYPAL", "BANK_TRANSFER", etc.
    private Double amount;
    private String transactionId;   // optional
    private boolean online;
}

