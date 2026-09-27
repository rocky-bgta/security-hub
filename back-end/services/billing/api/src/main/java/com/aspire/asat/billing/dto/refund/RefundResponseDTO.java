package com.aspire.asat.billing.dto.refund;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for refund")
public class RefundResponseDTO {

    @Schema(description = "Refund ID", example = "refund_789")
    private String id;

    @Schema(description = "Original payment ID", example = "payment_123")
    private String paymentId;

    @Schema(description = "Associated invoice ID", example = "invoice_456")
    private String invoiceId;

    @Schema(description = "Client ID", example = "client_001")
    private String clientId;

    @Schema(description = "MSP Admin ID", example = "msp_001")
    private String mspAdminId;

    @Schema(description = "Country ID", example = "usa")
    private String countryId;

    @Schema(description = "Refund amount", example = "100.00")
    private Double refundAmount;

    @Schema(description = "Currency", example = "USD")
    private String currency;

    @Schema(description = "Reason for refund", example = "Customer requested refund")
    private String reason;

    @Schema(description = "Refund status", example = "PROCESSED")
    private RefundStatus status;

    @Schema(description = "When the refund was processed")
    private Instant refundedAt;

    @Schema(description = "When the refund was created")
    private Instant createdAt;

    @Schema(description = "Admin who processed the refund")
    private String processedBy;

    @Schema(description = "Gateway transaction ID")
    private String transactionId;

    @Schema(description = "Additional notes")
    private String notes;
}

