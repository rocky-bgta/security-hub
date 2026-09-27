package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicListResponseDto {

    private String subPackageId;
    private String subPackageName;
    private List<TopicInfo> topics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicInfo {
        private String topicId;
        private String topicName;
        private String description;
        private int totalQuestions;
    }
}
