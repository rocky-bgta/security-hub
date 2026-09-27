package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.LandingPageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Step 3: Landing Page Selection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignLandingPageRequest {

    private String landingPageId;

    private LandingPageType landingPageType;

    /** Optional override of landing page tracking domain for this campaign. */
    private String trackingDomainId;
}
