package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AudienceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for Step 6: Audience Selection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAudienceRequest {

    @NotNull(message = "Audience type is required")
    private AudienceType audienceType;

    @Builder.Default
    private List<String> departmentIds = new ArrayList<>();

    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    @Builder.Default
    private List<String> userIds = new ArrayList<>();
}
