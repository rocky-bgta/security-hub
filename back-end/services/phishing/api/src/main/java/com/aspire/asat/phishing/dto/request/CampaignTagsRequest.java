package com.aspire.asat.phishing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for Step 5: Campaign Tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTagsRequest {

    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
