package com.aspire.asat.registration.data.phishing.response;

import com.aspire.asat.registration.data.phishing.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for user risk summary (matches Phishing service response).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskSummaryDto {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String department;

    private RiskLevel riskLevel;
    private Double riskScore;

    private int campaignsTargeted;
    private int emailsReceived;
    private int emailsOpened;
    private int emailsClicked;
    private int dataSubmissions;
    private int emailsReported;
    private int breachesInvolved;

    private boolean isRepeatOffender;
    private Instant lastActivityAt;
    private Instant lastClickedAt;
}
