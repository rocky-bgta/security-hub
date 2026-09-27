package com.aspire.asat.registration.data.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product license user onboarding report for a Client Admin")
public class ProductLicenseOnboardingReportDto {

    @Schema(description = "Sum of licenseCount from ACTIVE client_products", example = "20")
    private long totalLicenses;

    @Schema(description = "Sum of usedLicenseCount from ACTIVE client_products", example = "6")
    private long activeUsers;

    @Schema(description = "AspireUser count where isCredentialSent = false", example = "4")
    private long pendingActivation;

    @Schema(description = "Count of user_licence rows for the client admin", example = "10")
    private long assignedLicenses;

    @Schema(description = "Total licenses minus assigned licenses", example = "10")
    private long unusedLicenses;

    @Schema(description = "AspireUser count where status = SUSPEND", example = "0")
    private long suspendedAccounts;

    @Schema(description = "Total AspireUser count for the client admin (onboarded users)", example = "10")
    private long accountCreated;

    @Schema(description = "AspireUser count where isCredentialSent = true", example = "10")
    private long accountActivated;

    @Schema(description = "Count of end_user_packages for the client admin", example = "6")
    private long trainingAssigned;

    @Schema(description = "Count of COMPLETED user_subpackages from CMS", example = "0")
    private long trainingCompleted;

    @Schema(description = "Count of user_certificates from CMS", example = "0")
    private long certificateEarned;
}
