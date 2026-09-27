package com.aspire.asat.registration.data.mspUser.response;

import com.aspire.asat.registration.data.enums.MspStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspOnboardedListResponseDto {
    private String id;
    private String mspId;
    private String organizationName;
    private String contactEmail;
    private String phoneNumber;
    private MspStatus status;
    private int licenseCount;
    private int usedLicenseCount;
    private List<String> productLists;
    private Instant joinedDate;
    private Instant lastLoginAt;
    private int totalClients;
    private double revenue;
    private double creditAmount;
}
