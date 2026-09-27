package com.aspire.asat.cms.dto.userLicence;

import com.aspire.asat.cms.dto.enums.LicenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLicenceResponseDto {

    private String id;
    private String userId;
    private String clientAdminId;
    private String productId;
    private String packageId;
    private String subPackageId;
    private LicenceStatus licenceStatus;
    private Instant issueDate;
    private Instant expireDate;
}
