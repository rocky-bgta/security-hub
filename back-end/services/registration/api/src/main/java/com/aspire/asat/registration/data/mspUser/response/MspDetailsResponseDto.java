package com.aspire.asat.registration.data.mspUser.response;

import com.aspire.asat.registration.data.mspUser.request.BillingInfoForMspDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed MSP information response")
public class MspDetailsResponseDto {

    @Schema(description = "MSP ID", example = "msp-id-123")
    private String id;

    @Schema(description = "MSP ID (legacy)", example = "msp-id-123")
    private String mspId;

    @Schema(description = "Organization phone number", example = "1700000000")
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    @Schema(description = "Organization details")
    private OrganizationInfoForMspResponseDto organization;

    @Schema(description = "Billing contact details")
    private BillingInfoForMspDto billing;

    @Schema(description = "Credit allocation details")
    private CreditInfoResponseDto creditInfo;

    // Product and Package Information
    @Schema(description = "Product IDs")
    private List<String> productIds;

    @Schema(description = "Package IDs")
    private List<String> packageIds;

    @Schema(description = "Client product IDs")
    private List<String> clientProductIds;

    // Invoice Information
    @Schema(description = "Invoice ID", example = "invoice-id-123")
    private String invoiceId;

    @Schema(description = "Invoice status", example = "PENDING")
    private String invoiceStatus;

    @Schema(description = "Credit ID", example = "credit-id-123")
    private String creditId;

    @Schema(description = "Notes")
    private String notes;

    // Status and Metadata
    @Schema(description = "Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Created by user ID")
    private String createdBy;

    @Schema(description = "Created at timestamp")
    private Instant createdAt;

    @Schema(description = "Updated at timestamp")
    private Instant updatedAt;
}

