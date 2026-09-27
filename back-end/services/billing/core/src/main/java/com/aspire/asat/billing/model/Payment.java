package com.aspire.asat.billing.model;

import com.aspire.asat.billing.dto.DiscountedItemDTO;
import com.aspire.asat.billing.dto.PaymentSourceDTO;
import com.aspire.asat.billing.dto.invoice.PaymentFailureReason;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.dto.payment.request.PaymentType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String invoiceId;
    private Double amount;          // Grand total paid (sum of paymentSources.amount)
    private String currency;
    private String status;           // PENDING, SUCCESS, FAILED, CANCELLED
    private PaymentFailureReason failureReason;  // Only set when status = FAILED (for analytics)

    private boolean online;          // true if any payment source is online
    private boolean isActive;

    private String clientId;
    private String mspAdminId;      // MSP Admin ID from Invoice for filtering
    private String countryId;       // Country ID from Invoice for filtering
    private RoleType roleType;      // CLIENT or MSP - populated from Invoice when payment is created
    private String notes;

    private Instant paymentDate;
    private Instant createdAt = Instant.now();

    private String couponId;
    private String couponCode;
    private Double discountAmount;
    private Double actualAmount;
    private Double subtotal;
    private Double vatAmount;
    private String transactionId;


    private Object metaData;         // gateway webhook data

    private List<DiscountedItemDTO> breakdown;

    private List<PaymentSourceDTO> paymentSources;  // Multi-method partial support

    private String invoiceFileKey;   // S3 or Azure Blob Storage Key for generated invoice file (optional)

    private boolean receiptGenerated;  // Whether a receipt PDF has been generated and uploaded

    private PaymentType paymentType;  //

}
