package com.aspire.asat.registration.data.clientAdmin.response;

import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.cms.response.CmsProductResponseDto;
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
@Schema(description = "Detailed information about a client product with full product and package details")
public class ClientProductDetailedDto {

    @Schema(description = "Unique identifier of the client product", example = "client-product-uuid-123")
    private String id;

    @Schema(description = "Client admin ID", example = "admin-uuid-123")
    private String clientAdminId;

    @Schema(description = "Product ID", example = "product-xyz-001")
    private String productId;

    @Schema(description = "Package ID", example = "package-abc-123")
    private String packageId;

    @Schema(description = "Complete product details from CMS service")
    private CmsProductResponseDto product;

    @Schema(description = "Complete package details from CMS service")
    private CmsPackageDto packageDetails;

    @Schema(description = "Number of licenses", example = "10")
    private Integer licenseCount;

    @Schema(description = "Number of used licenses", example = "5")
    private Integer usedLicenseCount;

    @Schema(description = "Price per license", example = "99.99")
    private Double pricePerLicense;

    @Schema(description = "Total price", example = "999.90")
    private Double totalPrice;

    @Schema(description = "Validity period", example = "12")
    private Integer validityPeriod;

    @Schema(description = "Validity unit", example = "MONTH")
    private String validityUnit;

    @Schema(description = "Assignment timestamp", example = "2024-01-15T10:30:00Z")
    private Instant assignedAt;

    @Schema(description = "Expiry date", example = "2025-01-15T10:30:00Z")
    private Instant expiryDate;

    @Schema(description = "License status", example = "ACTIVE")
    private String licenseStatus;

    @Schema(description = "Payment payload information")
    private PaymentPayloadDto paymentPayload;

    @Schema(description = "Number of topics for this product-package combination", example = "2")
    private Integer topicCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Payment payload details")
    public static class PaymentPayloadDto {
        @Schema(description = "Client ID", example = "client-uuid-123")
        private String clientId;

        @Schema(description = "Currency", example = "USD")
        private String currency;

        @Schema(description = "Total amount", example = "999.90")
        private Double amount;

        @Schema(description = "Subtotal", example = "950.00")
        private Double subtotal;

        @Schema(description = "VAT amount", example = "49.90")
        private Double vatAmount;

        @Schema(description = "Discount amount", example = "0.00")
        private Double discountAmount;

        @Schema(description = "Discount percentage", example = "0.0")
        private Double discountPercentage;

        @Schema(description = "Payment date", example = "2024-01-15T10:30:00Z")
        private Instant date;

        @Schema(description = "Payment notes", example = "Invoice auto-generated during onboarding")
        private String notes;

        @Schema(description = "Client region", example = "Bangladesh")
        private String clientRegion;
    }
}
