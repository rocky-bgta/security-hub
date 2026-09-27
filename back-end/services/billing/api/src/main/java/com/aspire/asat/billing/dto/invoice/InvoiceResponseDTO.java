package com.aspire.asat.billing.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response structure for Invoice details")
public class InvoiceResponseDTO {

    @Schema(description = "Unique invoice ID", example = "INV-1751887035235")
    private String id;

    @Schema(description = "Client Admin ID associated with the invoice", example = "a1b2c3d4e5")
    private String clientAdminId;

    @Schema(description = "Client organization name", example = "Aspire Global")
    private String clientName;

    @Schema(description = "MSP Admin ID associated with the invoice", example = "msp-admin-123")
    private String mspAdminId;

    @Schema(description = "MSP organization name", example = "MSP Solutions Inc")
    private String mspName;

    @Schema(description = "Optional billing email address for the invoice", example = "billing@example.com")
    private String billingEmail;

    @Schema(description = "List of ClientProduct IDs linked to this invoice")
    private List<String> clientProductIds;

    @Schema(description = "Subtotal before discounts and VAT", example = "1000.00")
    private double subtotal;

    @Schema(description = "Type of discount applied (FLAT or PERCENTAGE)", example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(description = "Total discount amount applied", example = "100.00")
    private double discountAmount;

    @Schema(description = "Discount percentage applied", example = "10.0")
    private double discountPercentage;

    @Schema(description = "Coupon code applied to the invoice (if any)", example = "SUMMER10")
    private String couponCode;

    @Schema(description = "Coupon document ID applied to the invoice (if any)", example = "coupon-abc-123")
    private String couponId;

    @Schema(description = "Coupon validity end timestamp denormalized onto the invoice", example = "2026-06-10T23:59:59Z")
    private Instant couponValidUntil;

    @Schema(description = "Coupon discount amount applied", example = "50.00")
    private double couponDiscountAmount;

    @Schema(description = "VAT amount applied", example = "45.00")
    private double vatAmount;

    @Schema(description = "VAT percentage applied", example = "5.0")
    private double vatPercentage;

    @Schema(description = "Final total amount after applying discount and VAT", example = "945.00")
    private double totalAmount;

    @Schema(description = "Readable invoice download link (PDF)", example = "https://blob.azure.com/invoices/INV-12345.pdf")
    private String invoicePdfLink;

    @Schema(description = "Status of the invoice", example = "PAID")
    private InvoiceStatus status;

    @Schema(description = "Reason for invoice cancellation or expiry; null while invoice is active", example = "Payment not received within the due date")
    private String reason;

    @Schema(description = "Invoice creation timestamp", example = "2025-07-06T10:15:30Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the invoice was marked as paid", example = "2025-07-06T12:00:00Z")
    private Instant paidAt;

    @Schema(description = "Global unpaid invoice expiry timestamp", example = "2025-08-05T10:15:30Z")
    private Instant expiresAt;

    @Schema(description = "Selected products and packages for the invoice")
    private List<ProductSelectionDto> productSelections;

    @Schema(description = "Country ID", example = "usa")
    private String countryId;

    @Schema(description = "Country name (e.g., United States)", example = "United States")
    private String countryName;

    @Schema(description = "Role type associated with the invoice (CLIENT or MSP)", example = "CLIENT")
    private RoleType roleType;

    @Schema(description = "Payment method used for completed payment (BANK_TRANSFER or CHECK_PAYMENT)", example = "BANK_TRANSFER")
    private PaymentMethodType paymentMethod;

    @Schema(description = "Optional URL for bank receipt document", example = "https://s3.amazonaws.com/receipts/bank-receipt-123.pdf")
    private String bankReceiptUrl;

    @Schema(description = "Payment details (bank transfer or check payment details)")
    private Object paymentDetails;
}
