package com.aspire.asat.registration.data.mspUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credit allocation details for the MSP")
public class CreditInfoDto {

    @Schema(
            description = "Enable credit allocation for the MSP",
            example = "true"
    )
    private Boolean enableCredit;

    @DecimalMin(value = "0.0", inclusive = true, message = "Credit amount must be non-negative")
    @Schema(
            description = "Initial credit amount for the MSP (required if credit is enabled)",
            example = "1000.00"
    )
    private Double creditAmount;

    @Schema(
            description = "Reason for providing the credit (required if credit is enabled)",
            example = "Initial credit allocation for launch"
    )
    private String reason;


    @Schema(description = "Net payment days ID (for invoice settlement terms)", example = "net-days-id-123")
    private String netDaysId;

    @Schema(
            description = "Auto suspend account when payment is overdue",
            example = "true"
    )
    private Boolean autoSuspendOnOverdue;

    @Schema(
            description = "Credit start date (required if credit is enabled)",
            example = "2024-01-01"
    )
    private Date creditStartDate;

}
