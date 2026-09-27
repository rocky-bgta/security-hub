package com.aspire.asat.cms.dto.subPackage;

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
@Schema(description = "Assigned user details for a sub-package")
public class SubPackageAssignedUserDto {

    @Schema(description = "User ID", example = "user-uuid-123")
    private String userId;

    @Schema(description = "User email from AspireUser", example = "john.doe@company.com")
    private String email;

    @Schema(description = "User full name from AspireUser", example = "John Doe")
    private String fullName;

    @Schema(description = "User department from AspireUser", example = "IT")
    private String department;

    @Schema(description = "User risk group from AspireUser", example = "HIGH_RISK")
    private String riskGroup;

    @Schema(description = "Sub-package name", example = "Security Awareness")
    private String subPackageName;

    @Schema(description = "Assignment status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Assignment date", example = "2026-07-01")
    private LocalDate assignedAt;

    @Schema(description = "Expiry date", example = "2026-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Progress percentage", example = "45.0")
    private double progress;
}
