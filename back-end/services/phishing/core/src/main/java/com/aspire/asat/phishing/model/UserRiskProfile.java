package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB entity for user risk profiles.
 * Aggregated risk data per user across all campaigns.
 */
@Document(collection = "user_risk_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "client_user_idx", def = "{'clientId': 1, 'userId': 1}", unique = true),
    @CompoundIndex(name = "client_risk_idx", def = "{'clientId': 1, 'riskLevel': 1}")
})
public class UserRiskProfile {

    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private String userId;

    private String email;

    private String firstName;

    private String lastName;

    private String department;

    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.HIGH;

    @Builder.Default
    private Double riskScore = 0.0;

    @Builder.Default
    private Double phishingRiskScore = 0.0;

    @Builder.Default
    private Double trainingRiskScore = 0.0;

    private Boolean isTrainingEnabled;
    private Boolean isPhishingEnabled;

    @Builder.Default
    private int campaignsTargeted = 0;

    @Builder.Default
    private int emailsReceived = 0;

    @Builder.Default
    private int emailsOpened = 0;

    @Builder.Default
    private int linksClicked = 0;

    @Builder.Default
    private int dataSubmissions = 0;

    @Builder.Default
    private int emailsReported = 0;

    @Builder.Default
    private int breachesInvolved = 0;

    private Instant lastActivityAt;

    private Instant lastClickedAt;

    private Instant lastReportedAt;

    private Instant updatedAt;

    /**
     * Calculate risk level based on behavior
     * Sets risk level from the current numeric riskScore using 4-class bands:
     * Low (0–20), Medium (21–50), High (51–70), Critical (71–100).
     * Call after riskScore has been set.
     */
    public void calculateRiskLevel() {
        // BR-04: Repeat offender = clicked 3+ times
        // BR-05: High risk = clicked in 2+ campaigns

        if (dataSubmissions >= 2 || linksClicked >= 5) {
            this.riskLevel = RiskLevel.CRITICAL;
            this.riskScore = 90.0 + Math.min(10, dataSubmissions * 2);
        } else if (dataSubmissions >= 1 || linksClicked >= 3) {
            this.riskLevel = RiskLevel.HIGH;
            this.riskScore = 60.0 + Math.min(30, linksClicked * 5 + dataSubmissions * 10);
        } else if (linksClicked >= 1) {
            this.riskLevel = RiskLevel.MEDIUM;
            this.riskScore = 30.0 + Math.min(30, linksClicked * 10);
        } else {
            this.riskLevel = RiskLevel.LOW;
            this.riskScore = (double) Math.max(0, 10 - emailsReported * 2);
        }

        // Bonus for reporting phishing emails
        if (emailsReported > 0) {
            this.riskScore = Math.max(0.0, this.riskScore - emailsReported * 5);
        }
    }

    /**
     * Sets risk level from the current numeric riskScore using 4-class bands:
     * Low (0–20), Medium (21–50), High (51–70), Critical (71–100).
     * Call after riskScore has been set.
     */
    public void setRiskLevelFromScore() {
        double score = this.riskScore != null ? this.riskScore : 0.0;
        this.riskLevel = RiskScoreUtils.fromScore(score);
    }

    /**
     * Check if user is a repeat offender (clicked 3+ times)
     */
    public boolean isRepeatOffender() {
        return linksClicked >= 3;
    }
}
