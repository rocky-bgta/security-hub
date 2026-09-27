package com.aspire.asat.auth.entity;

import com.aspire.asat.auth.dto.enums.OnboardBy;
import com.aspire.asat.auth.dto.enums.RiskGroup;
import com.aspire.asat.auth.dto.enums.UserSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "aspire_user")
public class AspireUser {

    @Id
    private UUID id;

    private UUID userId;  // Reference to the original domain-specific user ID

    private String firstName;
    private String lastName;
    private String username;    // Set as email
    private String email;
    private String password;    // Encoded password
    private String phoneNumber;
    private String country;
    private String countryCode; // e.g., +1, +91
    private String address;
    private List<String> roles; // Role IDs from RoleRepository
    private String userType;    // CLIENT, ADMIN, MSP, USER
    private String status;      // ACTIVE, INACTIVE, TEMPORARY_BLOCKED, ENABLED, DISABLED
    private String createdBy;   // ID of the user who created this user
    private Instant createdAt;
    private Instant updatedAt;
    private String  updatedBy;
    private String clientAdminId; // For users associated with a specific client admin
    private String mspId; // For users associated with a specific MSP
    private String department; // Department of the user
    private RiskGroup riskGroup; // Risk group classification for the user
    private Instant lastLoginAt;
    private String companyName;
    private String designation; // Job designation/title
    private String supervisorName; // Name of the supervisor
    private String profilePicture;

    // MFA fields
    private Boolean mfaEnabled;
    private String mfaMethod;
    private String mfaSecret;
    private Instant mfaSetupAt;

    // Trial fields
    private Boolean isTrial; // Indicates if user is on trial
    private Instant trialStartDate; // When trial started
    private Instant trialEndDate; // When trial expires

    // Buy Now fields
    private Boolean isBuyNow;

    // Migration tracking field
    private String migratedFromClientAdminId; // Tracks the original trial ClientAdminId after migration
    private UserSource userSource;  // AD, MICROSOFT_ENTRA, GOOGLE
    // Admin inactive flag
    private Boolean isAdminInactive = false;
    private Boolean isDefault;
    private Instant  tempPasswordExpiry;
    private Boolean isCredentialSent;
    private Boolean isRiskProfileExist = false;
}
