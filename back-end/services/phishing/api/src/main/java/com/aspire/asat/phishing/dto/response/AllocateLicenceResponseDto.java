package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of phishing license allocation (preview or confirmed insert).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateLicenceResponseDto {

    private int licenseCount;
    private int usedLicenseCount;
    private int availableLicenseCount;

    private int selectedUserCount;
    private int existingLicensedUserCount;
    private int newLicenseRequiredCount;
    private int newLicenseAllocatedCount;

    private boolean requiresConfirmation;
    private String confirmationMessage;

    /** Users that may proceed for this campaign (capped when over limit). */
    @Builder.Default
    private List<String> userIds = new ArrayList<>();
}
