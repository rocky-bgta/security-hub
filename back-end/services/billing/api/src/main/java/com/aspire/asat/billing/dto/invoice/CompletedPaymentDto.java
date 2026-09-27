package com.aspire.asat.billing.dto.invoice;

import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing completed payment details including payment method and associated details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Completed payment details with payment method and associated information")
public class CompletedPaymentDto {

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method used for the completed payment", 
            example = "BANK_TRANSFER", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentMethodType paymentMethod;

    @Valid
    @Schema(description = "Bank transfer details (required when paymentMethod is BANK_TRANSFER)")
    private BankTransferDetailsDto bankTransferDetails;

    @Valid
    @Schema(description = "Check payment details (required when paymentMethod is CHECK_PAYMENT)")
    private CheckPaymentDetailsDto checkPaymentDetails;

    @Valid
    @Schema(description = "Optional comment log to be created with the invoice")
    private CommentLogRequestDTO commentLog;
}

