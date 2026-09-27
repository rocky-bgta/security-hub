package com.aspire.asat.phishing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phishing performance report aggregated from all {@code user_risk_profiles}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phishing performance percentages from user risk profiles")
public class PhishingPerformanceDto {

    @Schema(description = "Sum of emailsReceived across all user risk profiles", example = "1000")
    private long emailsReceived;

    @Schema(description = "Sum of emailsReported across all user risk profiles", example = "120")
    private long emailsReported;

    @Schema(description = "emailsReceived - emailsOpened (ignored / not opened)", example = "400")
    private long emailsIgnored;

    @Schema(description = "Sum of linksClicked across all user risk profiles", example = "80")
    private long linksClicked;

    @Schema(description = "Reported percentage of emailsReceived", example = "12.0")
    private double reportedPercentage;

    @Schema(description = "Ignored or not opened percentage of emailsReceived", example = "40.0")
    private double ignoredOrNotOpenedPercentage;

    @Schema(description = "Clicked (clicked / data submitted / compromised) percentage of emailsReceived", example = "8.0")
    private double clickedPercentage;
}
