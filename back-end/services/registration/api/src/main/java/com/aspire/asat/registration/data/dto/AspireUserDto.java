package com.aspire.asat.registration.data.dto;

import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.utils.PasswordSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for centralized user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AspireUserDto {

    private UUID baseUserId;  // Reference to the original domain-specific user ID
    private String firstName;
    private String lastName;
    private String username;    // Set as email
    private String email;
    private String password;    // Encoded password
    private String phoneNumber;
    private String phoneCode;   // e.g., +1, +880
    private String country;
    private String countryCode; // country ISO/code from dropdown
    private String address;
    private List<String> roles; // e.g., USER, ADMIN, MSP_ADMIN
    private String userType;    // CLIENT, ADMIN, MSP, USER
    private String status;      // ACTIVE, INACTIVE, TEMPORARY_BLOCKED, ENABLED, DISABLED
    private String createdBy;   // ID of the user who created this user
    private Instant createdAt;
    private Instant updatedAt;
    private String clientAdminId;
    private String department;
    private String companyName;
    private String designation; // Job designation/title
    private String supervisorName; // Name of the supervisor
    private RiskGroup riskGroup;
    private Boolean isRiskProfileExist;

    @JsonSerialize(using = PasswordSerializer.class)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private String plainPassword;

    private Instant lastLoginAt;

    private String profilePicture;

    private Boolean  isDefault;
    private Instant tempPasswordExpiry;
    private Boolean isCredentialSent;;

}

