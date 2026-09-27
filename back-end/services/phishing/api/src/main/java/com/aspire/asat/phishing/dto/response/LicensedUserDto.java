package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Licensed user row shaped like Registration EndUser for FE table reuse.
 * {@code id} is the Registration userId (not the licence document id).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicensedUserDto {

    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String phoneCode;
    private String department;
    private String organizationName;
    private String organizationDomain;
    private String countryName;
    private String countryCode;
    /** ACTIVE when licence snapshot active=true, else INACTIVE. */
    private String status;
    private String clientAdminId;
    private RiskGroup riskGroup;
    private String lastLoginAt;
    private String profilePicture;
    private Boolean isRiskProfileExist;
}
