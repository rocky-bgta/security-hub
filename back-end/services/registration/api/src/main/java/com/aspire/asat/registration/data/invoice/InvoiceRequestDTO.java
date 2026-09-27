package com.aspire.asat.registration.data.invoice;

import com.aspire.asat.registration.data.clientAdmin.request.CompletedPaymentDto;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.enums.DiscountType;
import com.aspire.asat.registration.data.enums.PaymentStatusType;
import com.aspire.asat.registration.data.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new invoice")
public class InvoiceRequestDTO {

    @Schema(description = "Client Admin ID", example = "client-admin-123")
    private String clientAdminId;

    @Schema(description = "MSP Admin ID", example = "msp-admin-123")
    private String mspAdminId;

    @Schema(description = "MSP Name", example = "MSP Solutions Inc")
    private String mspName;

    @Schema(description = "Client Name", example = "Aspire Global")
    private String clientName;

    @Schema(description = "Optional billing email address for the invoice", example = "billing@example.com")
    private String billingEmail;

    @NotEmpty
    @Schema(description = "List of associated client product IDs", example = "[\"cp-001\", \"cp-002\"]")
    private List<String> clientProductIds;

    @PositiveOrZero
    @Schema(description = "Subtotal before discounts and VAT", example = "1000")
    private double subtotal;

    @Schema(description = "Type of discount applied (FLAT or PERCENTAGE)", example = "PERCENTAGE")
    private DiscountType discountType;

    @PositiveOrZero
    @Schema(description = "Discount applied to the invoice", example = "100")
    private double discountAmount;

    @PositiveOrZero
    @Schema(description = "Discount percentage applied", example = "10")
    private double discountPercentage;

    @Schema(description = "Coupon code applied to the invoice (if any)", example = "SUMMER10")
    private String couponCode;

    @PositiveOrZero
    @Schema(description = "Coupon discount amount applied to the invoice", example = "50.00")
    private double couponDiscountAmount;

    @PositiveOrZero
    @Schema(description = "VAT amount applied", example = "50")
    private double vatAmount;

    @PositiveOrZero
    @Schema(description = "VAT rate in percentage used for calculation (e.g., 5.0 for 5%)", example = "5.0")
    private double vatRate;

    @Positive
    @Schema(description = "Total amount after discounts and VAT", example = "950")
    private double totalAmount;

    @Schema(description = "Optional note or status message", example = "Onboarding invoice for July")
    private String statusNote;

    @Schema(description = "Payment ID associated with this invoice (if any)", example = "payment-789")
    private String paymentId;

    @Schema(description = "Invoice PDF link (Azure Blob or S3 public link)", example = "https://cdn.aspire.com/invoices/INV-123456.pdf")
    private String invoicePdfLink;

    @Schema(description = "List of selected product and package metadata for the invoice")
    private List<ProductSelectionDto> productSelections;

    @Schema(description = "Country name (e.g., United States)", example = "United States")
    private String countryName;

    @Schema(description = "Country code (e.g., US)", example = "US")
    private String countryCode;

    @Schema(description = "Country ID (separate from countryCode)", example = "usa")
    private String countryId;

    @Schema(description = "State name (e.g., California)", example = "California")
    private String stateName;

    @Schema(description = "State code (e.g., CA)", example = "CA")
    private String stateCode;

    @Schema(description = "State ID (UUID format)", example = "ad30c701-9827-4fb7-b404-fac01fed5e78")
    private String stateId;

    @Schema(description = "Role type associated with the invoice (CLIENT or MSP)", example = "CLIENT")
    private RoleType roleType;

    @Schema(description = "Payment status (PENDING or COMPLETED)", example = "PENDING")
    private PaymentStatusType paymentStatus;

    @Schema(description = "Completed payment details (required when paymentStatus is COMPLETED)")
    private CompletedPaymentDto completedPayment;
}
