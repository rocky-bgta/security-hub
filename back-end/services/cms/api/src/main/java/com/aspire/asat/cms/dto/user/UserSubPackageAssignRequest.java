package com.aspire.asat.cms.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserSubPackageAssignRequest {
    
    @NotEmpty(message = "SubPackage data list cannot be empty")
    private List<SubPackageData> subPackageData;

    @Data
    public static class SubPackageData {
        @NotBlank(message = "SubPackage ID is required")
        private String subPackageId;

        @NotEmpty(message = "User IDs list cannot be empty")
        private List<String> userIdList;

        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotBlank(message = "Client admin ID is required")
        private String clientAdminId;

        @NotBlank(message = "Status is required")
        private String status;

        @NotNull(message = "ValidFor is required")
        private Long validFor;

        // ProductPackageId (ClientProduct ID) - optional field for tracking
        private String productPackageId;

        /** Optional delivery channel (e.g. EMAIL, SMS, VOICE). */
        private String channel;

        // Email notification fields
        private boolean enableFirstUserNotificationEmail;
        private List<String> secondaryEmails;
        private List<String> thirdLevelEmails;
        private List<String> fourthHREmails;
        private CompletionDays completionDays;
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
