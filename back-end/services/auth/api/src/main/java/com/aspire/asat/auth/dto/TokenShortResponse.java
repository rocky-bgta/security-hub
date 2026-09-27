package com.aspire.asat.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class TokenShortResponse implements Serializable {
    private String accessToken;
    private String refreshToken;
    private boolean deviceBindingNeeded;
    private boolean credentialChangeNeeded;
    private Instant lastLoginTime;
    private List<String> roleNames;
    
    // MFA fields
    private Boolean mfaSetupRequired; // User needs to set up MFA
    private Boolean mfaVerificationRequired; // User needs to verify MFA code
    private String mfaMethod; // Default enrolled method (frontend auto-select)
    private List<UserMfaMethodItem> methods; // Enrolled methods; user may verify with any of these
    private String tempToken; // Temporary token for MFA / forced credential-change flows
    
    // Trial fields
    private Integer trialDaysLeft; // Remaining days in trial period (null for non-trial users)
    
    // Buy Now field
    private Boolean isBuyNow; // Indicates if user signed up via buy-now flow
    
    // OnboardBy field
    private String onboardBy; // Values: TRIAL, BUY_NOW, MSP, ASPIRE_ADMIN, or null
}
