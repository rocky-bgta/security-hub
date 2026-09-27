package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommissionRateResponseDTO {

    private String id;
    private String clientId;
    private String mspId;
    private Double commissionPercentage;
    private Map<String, Object> eligibilityCriteria;
    private Double minInvoiceAmount;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
