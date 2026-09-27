package com.aspire.asat.registration.data.subpackage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubPackageAssignRequest {
    private List<SubPackageData> subPackageData;

    @Data
    public static class SubPackageData {
        @NotBlank(message = "SubPackage ID is required")
        private String subPackageId;

        @NotEmpty(message = "End user IDs list cannot be empty")
        private List<String> userIdList;

        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotBlank(message = "Package ID is required")
        private String packageId;

        @NotBlank(message = "Product Package ID is required")
        private String productPackageId;

        @NotBlank(message = "Client admin ID is required")
        private String clientAdminId;

        @NotBlank(message = "Status is required")
        private String status;

        @NotNull(message = "ValidFor is required")
        private Long validFor;

        /** Optional delivery channel (e.g. EMAIL, SMS, VOICE). */
        private String channel;

        // Email notification fields
        private boolean enableFirstUserNotificationEmail;
        private List<String> secondaryEmails;
        private List<String> thirdLevelEmails;
        private List<String> fourthHREmails;
        @NotNull(message = "CompletionDays is required")
        @Valid
        private CompletionDays completionDays;

        // notification data
        private String subPackageName;
        private String productName;
    }

    @Data
    public static class CompletionDays {
        private DurationUnit durationUnit;
        private Integer durationValue;
    }

    public enum DurationUnit {
        DAYS, WEEKS, MONTHS
    }

}