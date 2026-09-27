package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Indicates whether the authenticated end user has been enrolled in any phishing campaign,
 * and which delivery channels those campaigns use.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndUserPhishingVisibilityDto {

    /** True when at least one campaign_recipients row exists for this user (any channel). */
    private boolean hasReceivedCampaign;

    /** True when the user is enrolled in at least one EMAIL campaign. */
    private boolean hasReceivedEmailCampaign;

    /** True when the user is enrolled in at least one SMS campaign. */
    private boolean hasReceivedSmsCampaign;

    /** True when the user is enrolled in at least one VOICE campaign. */
    private boolean hasReceivedVoiceCampaign;
}
