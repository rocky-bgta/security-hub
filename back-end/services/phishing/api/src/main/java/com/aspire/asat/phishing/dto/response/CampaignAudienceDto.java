package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.AudienceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for campaign audience data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAudienceDto {

    private AudienceType type;
    private List<String> departmentIds;
    private List<String> departmentNames;
    private List<String> groupIds;
    private List<String> groupNames;
    private List<String> userIds;
    private int recipientCount;
}
