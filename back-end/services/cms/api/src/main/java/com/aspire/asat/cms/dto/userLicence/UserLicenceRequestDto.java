package com.aspire.asat.cms.dto.userLicence;

import com.aspire.asat.cms.dto.enums.LicenceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLicenceRequestDto {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Client Admin ID is required")
    private String clientAdminId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Package ID is required")
    private String packageId;

    @NotBlank(message = "SubPackage ID is required")
    private String subPackageId;

    @NotNull(message = "License Status is required")
    private LicenceStatus licenceStatus;

    @NotNull(message = "Issue Date is required")
    private Instant issueDate;

    @NotNull(message = "Expire Date is required")
    private Instant expireDate;
}
