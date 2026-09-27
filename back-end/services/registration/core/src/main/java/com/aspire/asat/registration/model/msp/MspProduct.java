package com.aspire.asat.registration.model.msp;

import com.aspire.asat.registration.data.EmbeddedPaymentPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for storing MSP product selections
 * Created during MSP onboarding process
 */
@Document(collection = "msp_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspProduct {

    private String id; // UUID as String
    private String mspId; // MSP admin ID

    private String productId;
    private String packageId;

    private int licenseCount;
    private int usedLicenseCount;

    private double pricePerLicense;
    private double totalPrice;

    private int validityPeriod;
    private String validityUnit; // MONTH, YEAR

    private Instant assignedAt;
    private Instant expiryDate;

    private String licenseStatus; // e.g., PENDING, ACTIVE

    private EmbeddedPaymentPayload paymentPayload;
    private String countryId;
}

