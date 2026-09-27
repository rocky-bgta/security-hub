package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated phishing campaign participation statistics for an end user (USER role).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCampaignStatisticsDto {

    /** Campaigns the user is enrolled in for the requested channel. */
    private long totalCampaigns;

    /** Recipients in an opened/answered (or later) status for the requested channel. */
    private long openCount;

    /** Recipients in a clicked/engaged (or later) status for the requested channel. */
    private long clickCount;

    /** Recipients in a compromised/data-submitted status for the requested channel. */
    private long compromiseCount;

    /** Recipients who reported the simulation for the requested channel. */
    private long reportCount;
}
