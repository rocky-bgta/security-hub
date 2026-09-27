package com.aspire.asat.auth.dto.apiResponses;

import com.aspire.asat.auth.dto.enums.RiskGroup;
import com.aspire.asat.common.dto.files.RoleData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenResponse implements Serializable {
    // login response
    private String accessToken;
    private String refreshToken;
    private boolean deviceBindingNeeded;
    private boolean credentialChangeNeeded;
    private Instant lastLoginTime;
    private List<RoleData> roles; // User roles with ID and name




    // Details for token generation and storage
    private String tokenId; // UUID for JWT subject and Redis key

    private String userId;
    private String clientAdminId;
    private String mspId;
    private String countryId;
    private String userType;

    private String email;
    private String username;
    private String phoneNumber;
    private String userStatus;
    private String fullName;
    private String clientAdminEmail;
    private String clientAdminFullName;
    private RiskGroup riskGroup; // Risk group classification for the user

    private String passwordExpiryDate;

    private List<String> scope;

    // Buy Now field
    private Boolean isBuyNow; // Indicates if user signed up via buy-now flow
    private String onboardBy;
}
