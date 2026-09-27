package com.aspire.asat.cms.dto.license;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "One user–product license assignment row for the License Assignments table")
public class LicenseAssignmentRowDto {

    @Schema(description = "End user ID", example = "a1e35989-5081-447e-ada1-25b978ea316e")
    private String userId;

    @Schema(description = "User full name", example = "John Smith")
    private String fullName;

    @Schema(description = "User email", example = "john.smith@crosstewart.com")
    private String email;

    @Schema(description = "User department", example = "Human Resources")
    private String department;

    @Schema(description = "Parent package ID", example = "pkg-gold")
    private String packageId;

    @Schema(description = "Parent package / bundle name", example = "Gold")
    private String packageName;

    @Schema(description = "Product ID", example = "prod-sat")
    private String productId;

    @Schema(description = "Product name", example = "Security Awareness Training")
    private String productName;

    @Schema(description = "Sub-package ID", example = "sub-1")
    private String subPackageId;

    @Schema(description = "Sub-package name", example = "Security Awareness Training")
    private String subPackageName;

    @Schema(description = "Assignment date", example = "2026-04-26")
    private LocalDate assignedDate;

    @Schema(description = "Expiry date", example = "2027-04-26")
    private LocalDate expiryDate;

    @Schema(description = "Display status: Active, Expiring, Expired, or Complete", example = "Active")
    private String status;
}
