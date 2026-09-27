package com.aspire.asat.phishing.dto;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request body for creating or updating a UserRiskProfile.
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

    /** When true, save() will set/update isTrainingEnabled (e.g. when called from UserRiskProfileController). */
    private Boolean fromTrainingContext;
    /** When true, save() will set/update isPhishingEnabled to true (e.g. when called from campaign recipient creation). */
    private Boolean fromPhishingContext;

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
