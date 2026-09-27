package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AssignedPackageOverviewDTO {
    private String packageName;
    private String productName;
    private String status;
    private String licenseExpiry;
//    private int certificatesIssued;
    private int licenseUsed;
    private int licenseCount;
}
