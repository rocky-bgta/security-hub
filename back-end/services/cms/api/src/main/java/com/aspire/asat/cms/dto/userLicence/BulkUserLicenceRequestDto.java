package com.aspire.asat.cms.dto.userLicence;

import com.aspire.asat.cms.dto.enums.LicenceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class BulkUserLicenceRequestDto {
    
    @NotEmpty(message = "User licence data list cannot be empty")
    private List<UserLicenceData> userLicenceData;

    @Data
    public static class UserLicenceData {
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
}
