package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Asset inventory counts for the client admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardAssetCountsDto {

    private int numberOfEmailTemplates;
    private int numberOfLandingPages;
    private int numberOfSenderProfiles;
}
