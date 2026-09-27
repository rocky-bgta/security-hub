package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO containing check payment details for completed payments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Check payment details for completed payments")
public class CheckPaymentDetailsDto {

    @NotBlank(message = "Check number is required")
    @Schema(description = "Check number", example = "CHK-789012", requiredMode = Schema.RequiredMode.REQUIRED)
    private String checkNumber;

    @NotBlank(message = "Bank name is required")
    @Schema(description = "Name of the bank", example = "Bank of America", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankName;

    @NotBlank(message = "Branch name is required")
    @Schema(description = "Name of the bank branch", example = "Downtown Branch", requiredMode = Schema.RequiredMode.REQUIRED)
    private String branchName;

    @NotNull(message = "Payment date is required")
    @Schema(description = "Date of the payment", example = "2025-12-07", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate paymentDate;

    @NotBlank(message = "Check image URL is required")
    @Schema(description = "URL of the uploaded check image or receipt (image or PDF, max 5MB)", 
            example = "https://s3.amazonaws.com/checks/chk-789.jpg", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String checkImageUrl;
}

