package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedPaymentPayload {

    private String clientId;
    private Double amount;
    private String currency;
    private Date date;
    private String notes;

    private List<EmbeddedPackageItem> packageItems;

    private String couponId;
    private String clientRegion;

    private List<EmbeddedPaymentSource> paymentSources;

    private Double subtotal;
    private Double vatAmount;
    private Double discountAmount;
    private Double discountPercentage;
}
