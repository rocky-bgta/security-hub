package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicExamInfoDto {
    
    private String topicId;
    private String topicName;
    private Integer questionCount;
    private Integer totalAvailableQuestions;
}
