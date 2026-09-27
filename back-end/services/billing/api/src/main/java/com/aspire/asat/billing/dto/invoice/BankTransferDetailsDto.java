package com.aspire.asat.billing.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO containing bank transfer payment details for completed payments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bank transfer payment details for completed payments")
public class BankTransferDetailsDto {

    @NotBlank(message = "Bank name is required")
    @Schema(description = "Name of the bank", example = "Chase Bank", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankName;

    @NotBlank(message = "Account number is required")
    @Schema(description = "Bank account number", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountNumber;

    @NotBlank(message = "Bank branch name is required")
    @Schema(description = "Name of the bank branch", example = "Main Branch", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankBranchName;

    @NotBlank(message = "Transaction number is required")
    @Schema(description = "Transaction reference number", example = "TXN-123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transactionNumber;

    @NotNull(message = "Payment date is required")
    @Schema(description = "Date of the payment", example = "2025-12-07", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate paymentDate;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    @Schema(description = "Amount paid via bank transfer", example = "225.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double paymentAmount;

    @NotBlank(message = "Transaction receipt URL is required")
    @Schema(description = "URL of the uploaded transaction receipt (image or PDF)", 
            example = "https://s3.amazonaws.com/receipts/txn-123.pdf", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String transactionReceiptUrl;
}

