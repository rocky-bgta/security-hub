package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.AudienceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Embedded model for campaign audience configuration.
 * Defines who receives the phishing campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAudience {

    private AudienceType type;

    @Builder.Default
    private List<String> departmentIds = new ArrayList<>();

    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    @Builder.Default
    private List<String> userIds = new ArrayList<>();

    private int recipientCount;
}
