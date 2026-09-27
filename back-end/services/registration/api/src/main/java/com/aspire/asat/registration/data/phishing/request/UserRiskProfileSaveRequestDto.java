package com.aspire.asat.registration.data.phishing.request;

import com.aspire.asat.registration.data.phishing.enums.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request body for creating or updating a UserRiskProfile via Phishing service.
 * clientId and userId are required; other fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskProfileSaveRequestDto {

    private String id;

    @NotBlank(message = "clientId is required")
    private String clientId;

    @NotBlank(message = "userId is required")
    private String userId;

    private String email;
    private String firstName;
    private String lastName;
    private String department;

    private RiskLevel riskLevel;
    private Double riskScore;
    private Double phishingRiskScore;
    private Double trainingRiskScore;

    private Boolean isTrainingEnabled;
    private Boolean isPhishingEnabled;

    private Integer campaignsTargeted;
    private Integer emailsReceived;
    private Integer emailsOpened;
    private Integer linksClicked;
    private Integer dataSubmissions;
    private Integer emailsReported;
    private Integer breachesInvolved;

    private Instant lastActivityAt;
    private Instant lastClickedAt;
    private Instant lastReportedAt;
}
