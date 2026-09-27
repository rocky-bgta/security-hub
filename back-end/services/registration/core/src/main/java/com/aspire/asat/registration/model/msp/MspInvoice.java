package com.aspire.asat.registration.model.msp;

import com.aspire.asat.registration.data.invoice.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document for storing MSP invoice information
 * Created during MSP onboarding process
 */
@Document(collection = "msp_invoice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspInvoice {

    @Id
    private String id;

    private String mspId;
    private String mspAdminEmail;

    private List<String> clientProductIds;

    private double subtotal;
    private double discountAmount;
    private double discountPercentage;
    private double vatAmount;
    private double vatPercentage;
    private double totalAmount;

    private InvoiceStatus status;
    private String statusNote;
    private String paymentId;
    private String couponCode;

    private String invoiceUrl;

    private Instant paidAt;

    private String createdBy;
    @CreatedDate
    private Instant createdAt;

    private String updatedBy;
    @LastModifiedDate
    private Instant updatedAt;
}
