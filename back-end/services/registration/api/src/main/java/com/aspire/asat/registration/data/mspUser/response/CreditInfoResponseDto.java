package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credit allocation details response with ID-Name pairs")
public class CreditInfoResponseDto {

    @Schema(description = "Enable credit allocation for the MSP", example = "true")
    private Boolean enableCredit;

    @Schema(description = "Initial credit amount for the MSP", example = "1000.00")
    private Double creditAmount;

    @Schema(description = "Reason for providing the credit", example = "Initial credit allocation for launch")
    private String reason;

    @Schema(description = "Net payment days information with ID and name")
    private IdNameDto netDays;

    @Schema(description = "Auto suspend account when payment is overdue", example = "true")
    private Boolean autoSuspendOnOverdue;

    @Schema(description = "Credit start date", example = "2024-01-01")
    private Date creditStartDate;

    @Schema(description = "Credit end/expiration date", example = "2025-01-01")
    private Date creditEndDate;
}

