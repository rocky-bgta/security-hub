package com.aspire.asat.auth.entity;

import com.aspire.asat.auth.dto.enums.AdminStatus;
import com.aspire.asat.auth.dto.enums.OnboardBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Minimal ClientAdmin entity for querying status from client_admins collection
 * This is used to check if a CLIENT_ADMIN user has ACTIVE status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "client_admins") // This is only read table, Don't update anything from here
public class ClientAdmin {

    private String id; // UUID as String

    private String email;
    private String hashedPassword;

    private String organizationName;
    private String contactEmail;
    private String phoneNumber;
    private String billingName;
    private String billingEmail;
    private String mspId;

    private String country;
    private String countryCode;   // e.g., "us" - kept for backward compatibility

    private String state;
    private String stateCode;     // e.g., "CA"

    private String timeZone;
    private String language;
    private String industry;
    private String domain;
    private String organizationSize;
    private String organizationType;

    // Organization address fields
    private String streetAddress;
    private String streetAddressLine2;
    private String city;
    private String zipPostalCode;

    private String logoUrl;

    private List<String> clientProductIds;

    private AdminStatus status; // PENDING, ACTIVE, etc.
    private Instant createdAt;

    private String clientAdminId;
    private OnboardBy onboardBy;
}

