package com.aspire.asat.billing.model;

import com.aspire.asat.billing.dto.invoice.DiscountType;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.PaymentMethodType;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import com.aspire.asat.billing.dto.invoice.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    private String id; // e.g., "INV-676603-775"

    private String clientAdminId;
    private String mspAdminId;
    private String clientName;
    private String mspName;
    private String billingEmail;  // Optional billing email address
    private RoleType roleType;  // CLIENT or MSP

    private List<String> clientProductIds;

    private double subtotal;
    private DiscountType discountType;  // FLAT or PERCENTAGE
    private double discountAmount;
    private double discountPercentage;
    private String couponCode;  // Optional coupon code applied to invoice
    private double couponDiscountAmount;  // Coupon discount amount applied to invoice
    private String couponId;              // Reserved coupon document id (billing-internal)
    private boolean couponUsageReserved;  // True after atomic reserve at invoice create
    private Instant couponReservedAt;     // When coupon usage was reserved (audit only; not used for cancel)
    private Instant couponValidUntil;     // Denormalized from Coupon.validUntil; cancel unpaid invoices after this
    private double vatAmount;
    private double vatRate;    // VAT rate in percentage used at invoice creation (e.g., 5.0 for 5%)
    private double totalAmount;

    private String statusNote;       // Optional notes like "Onboarding invoice"
    private String reason;           // Cancel or expire cause; null while invoice is active
    private InvoiceStatus status;    // PENDING, ON_PROGRESS, PAID, PARTIAL, OVERDUE, CANCELLED, EXPIRED

    private String invoicePdfLink;   // Public Azure Blob or S3 link to invoice PDF
    private String paymentId;        // Optional: to link directly to payment

    private Instant createdAt;
    private Instant paidAt;
    /** Global unpaid invoice expiry (createdAt + billing.invoice.expiry.days). */
    private Instant expiresAt;
    private List<ProductSelectionDto> productSelections; //for both client admin and msp admin

    private String countryName;  // e.g., "United States"
    private String countryCode;  // e.g., "US"
    private String countryId;    // Country ID (separate from countryCode)
    private String stateName;    // e.g., "California"
    private String stateCode;    // e.g., "CA"
    private String stateId;      // State ID (UUID format)

    // Payment details for completed payments
    private PaymentMethodType paymentMethod;  // BANK_TRANSFER or CHECK_PAYMENT
    private String bankReceiptUrl;            // Optional URL for bank receipt document
    private Object paymentDetails;            // Embedded document for bank/check details

}
