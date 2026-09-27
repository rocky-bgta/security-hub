package com.aspire.asat.auth.dto;

import com.aspire.asat.auth.dto.enums.OnboardBy;
import com.aspire.asat.auth.dto.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserDetailsResponse implements Serializable {
    private String userId;
    private String clientAdminId;
    private String email;
    private String username;
    private String phoneNumber;
    private String userStatus;
    private String fullName;
    private RiskGroup riskGroup;
    private Instant passwordExpiryDate;
    private String profilePicture;
    private List<String> clientProductTags;
    private Boolean selfOnboardingUser;
    private Boolean pendingPayment;  // Optional: true if client admin has only PENDING invoices, false if has PAID invoices, null for non-CLIENT_ADMIN users
    private OnboardBy onboardBy; // Values: TRIAL, BUY_NOW, or null for regular users
    /** Timezone reference from client_admins (e.g. registration dropdown timezone id). */
    private String timeZone;
    /** Purchased products for CLIENT_ADMIN; null for other user types. */
    private List<PurchaseProductDto> purchaseProducts;
}
