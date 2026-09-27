package com.aspire.asat.registration.data.endUser.response;

import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.enums.RiskGroup;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EndUserResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String fullName; // Keep for backward compatibility, will be constructed from firstName + lastName
    private String email;
    private String phoneNumber;
    private String phoneCode;
    private String department;
    private String organizationName;
    private String organizationDomain;
    private String countryName;
    private String countryCode;
    private UserStatus status;
    private String clientAdminId;
    private RiskGroup riskGroup;
    private Instant lastLoginAt;
    private String profilePicture;
    private String errorReason;
    /** True if this user has a risk profile in the Phishing service for the client. */
    private Boolean isRiskProfileExist;
}
