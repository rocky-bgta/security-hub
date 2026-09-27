package com.aspire.asat.billing.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditUpdateRequestDTO {
    private Double creditAmount;
    private Instant expirationDate;
    private String reason;
    private Boolean active;
}
