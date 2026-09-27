package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.TopicIdDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Step 7: Training module (sub-package creation via CMS when {@code PHISHING_WITH_TRAINING}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTrainingRequest {

    private String name;
    private String description;
    private String productId;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private List<String> topicId;
    private List<TopicIdDetailDto> topicIdDetails;
    private CompletionDays completionDays;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionDays {
        private String durationUnit;
        private Integer durationValue;
    }
}
