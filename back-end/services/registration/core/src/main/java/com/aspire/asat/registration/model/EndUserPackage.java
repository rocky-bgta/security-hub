package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "end_user_packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndUserPackage {

    private String id;

    private String userId;
    private String clientAdminId;
    private String productId;
    private String subPackageId;

    private double progress; // 0 to 100
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    private Instant assignedAt;
    private Instant lastUpdated;
    private Instant expiryDate;
    private boolean active;

    // Email notification fields
    private boolean enableFirstUserNotificationEmail;
    private List<String> secondaryEmails;
    private List<String> thirdLevelEmails;
    private List<String> fourthHREmails;
    private CompletionDays completionDays;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionDays {
        private DurationUnit durationUnit;
        private Integer durationValue;
    }

    public enum DurationUnit {
        DAYS, WEEKS, MONTHS
    }
}
