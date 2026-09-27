package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot of Registration end-user fields to sync onto {@code phishing_user_licence} rows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLicenceSnapshotRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String clientAdminId;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String departmentName;
    private String countryName;
    private Boolean active;
}
