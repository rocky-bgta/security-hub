package com.aspire.asat.phishing.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phishing-local mirror of registration {@code POST /end-user/assign-sub-package} request body.
 * No Gradle dependency on the registration module.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPackageAssignRequestDto {

    private List<SubPackageData> subPackageData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubPackageData {
        private String subPackageId;
        private List<String> userIdList;
        private String productId;
        private String packageId;
        private String productPackageId;
        private String clientAdminId;
        private String status;
        private Long validFor;
        /** Optional delivery channel (e.g. EMAIL, SMS, VOICE). */
        private String channel;
        private CompletionDays completionDays;
        private boolean enableFirstUserNotificationEmail;
        private List<String> secondaryEmails;
        private List<String> thirdLevelEmails;
        private List<String> fourthHREmails;
        private String subPackageName;
        private String productName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionDays {
        private String durationUnit;
        private Integer durationValue;
    }
}
