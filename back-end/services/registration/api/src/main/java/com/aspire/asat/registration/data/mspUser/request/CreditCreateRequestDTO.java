package com.aspire.asat.registration.data.mspUser.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class CreditCreateRequestDTO {
    @NotBlank
    private String clientId;

    @Min(0)
    private Double creditAmount;

    private Date expirationDate;
    private String reason; // Optional notes or description
    private String addedBy;
}
