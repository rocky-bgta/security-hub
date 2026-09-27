package com.aspire.asat.cms.dto.clientDashboard;

import com.aspire.asat.cms.dto.enums.RiskCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

/**
 * DTO for individual user risk analysis details
 * Provides detailed information about a user's risk status and progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskDetailDto {

    private String userId;
    private String userName;
    private String userEmail;
    private RiskCategory riskCategory; // SAFE, LOW_RISK, AVERAGE_RISK, HIGH_RISK
    private Double overallProgress;
    private Integer totalSubPackages;
    private Integer completedSubPackages;
    private Integer inProgressSubPackages;
    private Integer notStartedSubPackages;
    private Integer examReadySubPackages;
    private LocalDate lastActivityDate;
    private Instant lastSynced;
    private List<String> expiredSubPackages;
    private List<String> expiringSoonSubPackages; // Expiring within 7 days
    private Integer totalExamAttempts;
    private Integer passedExams;
    private Integer failedExams;
}
