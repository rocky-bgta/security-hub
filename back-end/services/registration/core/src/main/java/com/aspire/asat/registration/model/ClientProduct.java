package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.EmbeddedPaymentPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "client_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProduct {

    private String id; // UUID as String
    private String clientAdminId;

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

    private String mspId; // MSP admin ID
    private String countryId;

    private String userRangeId; // User range ID for the package (e.g. from buy-now selection)
}
