package com.aspire.asat.cms.dto.clientDashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * DTO for User Risk Analysis Report Response
 * Provides risk categorization of users based on their training completion progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskAnalysisResponseDto {

    @NotNull(message = "Safe users count cannot be null")
    @Min(value = 0, message = "Safe users count cannot be negative")
    private Long safeUsers;

    @NotNull(message = "Low risk users count cannot be null")
    @Min(value = 0, message = "Low risk users count cannot be negative")
    private Long lowRisk;

    @NotNull(message = "Average risk users count cannot be null")
    @Min(value = 0, message = "Average risk users count cannot be negative")
    private Long averageRisk;

    @NotNull(message = "High risk users count cannot be null")
    @Min(value = 0, message = "High risk users count cannot be negative")
    private Long highRisk;

    /**
     * Total number of users analyzed
     */
    @NotNull(message = "Total users count cannot be null")
    @Min(value = 0, message = "Total users count cannot be negative")
    private Long totalUsers;

    /**
     * Risk analysis generation timestamp
     */
    private String generatedAt;

    /**
     * Client admin ID for which this analysis was generated
     */
    private String clientAdminId;

    /**
     * Product ID for which this analysis was generated (optional, null if analyzing all products)
     */
    private String productId;
}
