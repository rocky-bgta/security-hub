package com.aspire.asat.registration.data.cms.request;

import com.aspire.asat.registration.data.enums.LicenceStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class CmsUserLicenceRequestDto {
    
    private String userId;
    private String clientAdminId;
    private String productId;
    private String packageId;
    private String subPackageId;
    private LicenceStatus licenceStatus;
    private Instant issueDate;
    private Instant expireDate;
}
