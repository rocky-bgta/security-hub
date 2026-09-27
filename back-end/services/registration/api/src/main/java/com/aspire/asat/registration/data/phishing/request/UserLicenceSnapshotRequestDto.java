package com.aspire.asat.registration.data.phishing.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body for Phishing internal PUT /user-licence-snapshot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLicenceSnapshotRequestDto {

    private String userId;
    private String clientAdminId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String departmentName;
    private String countryName;
    private Boolean active;
}
