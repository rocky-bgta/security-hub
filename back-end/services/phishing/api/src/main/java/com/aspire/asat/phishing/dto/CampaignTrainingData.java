package com.aspire.asat.phishing.dto;

import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTrainingData {

    private String trainingModuleId;
    private String name;
    private String description;
    private String productId;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private List<String> topicId;
    private SubPackageAssignedFor assignedFor;
    private CompletionDays completionDays;

    /** Resolved from CMS for {@link #topicId}; {@code topicName} is null when the topic is not found. */
    private List<TopicIdDetailDto> topicIdDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionDays {
        private String durationUnit;
        private Integer durationValue;
    }
}
