package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "user_subpackages")
@CompoundIndex(name = "user_subpackage_idx", def = "{'userId': 1, 'subPackageId': 1}", unique = true)
public class UserSubPackage {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String subPackageId;

    @Indexed
    private String clientAdminId;

    private String productId;

    private String subPackageName;

    @Indexed
    private String status; // IN_PROGRESS, NOT_STARTED, COMPLETED, EXAM

    private Double riskScore;

    private String validity; // Optional: example "365 Days"

    private LocalDate assignedDate;  // NEW: when subpackage was assigned

    private LocalDate expiryDate;    // UPDATED: explicit expiry instead of calculated

    private List<String> completedTopicIds;

    private String certificateLink;        // NEW: PDF certificate link

    private String imageCertificateLink;   // NEW: Image certificate link

    // ✅ NEW FIELDS
    private double progress;               // overall subpackage progress in percentage

    private Instant lastSynced;            // last sync timestamp for progress update

    private String userEmail;
    private List<String> secondaryEmails; // to send reminders
    private List<String> thirdLevelEmails;
    private List<String> fourthHREmails;

    // Reminder tracking fields to prevent duplicate notifications
    private boolean reminder50PercentSent = false;
    private boolean reminder20PercentSent = false;
    private boolean reminder10PercentSent = false;
    private boolean reminderNotStartedSent = false;
    private boolean reminderExpirySent = false;
    private Boolean isTrial = false;
    private Boolean isPhishingSubpackage = false;

    /** Optional delivery channel (e.g. EMAIL, SMS, VOICE) when assigned from phishing. */
    private String channel;

    /**
     * Sets riskScore from current status (Scenario A – Training module).
     * Completed=0, Ongoing (EXAM/IN_PROGRESS)=40, Not Started=70, Overdue &gt; 30 days=100.
     * Phishing subpackages never store a training risk score (always null).
     * Call before save when status or expiryDate has been set or changed.
     */
    public void setRiskScoreFromStatus() {
        if (Boolean.TRUE.equals(isPhishingSubpackage)) {
            this.riskScore = null;
            return;
        }
        if (status == null) {
            return;
        }
        // Overdue > 30 days: training significantly past deadline → highest risk
        if (expiryDate != null && ("NOT_STARTED".equals(status) || "IN_PROGRESS".equals(status) || "EXAM".equals(status))) {
            if (expiryDate.plusDays(30).isBefore(LocalDate.now())) {
                this.riskScore = 100.0;
                return;
            }
        }
        switch (status) {
            case "COMPLETED" -> this.riskScore = 0.0;
            case "EXAM", "IN_PROGRESS" -> this.riskScore = 40.0;
            case "NOT_STARTED" -> this.riskScore = 70.0;
            default -> this.riskScore = 40.0;
        }
    }
}
