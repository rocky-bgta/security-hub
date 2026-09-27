package com.aspire.asat.cms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicAssignmentProgressRowDTO {
    private String topicId;
    private String topicName;
    private Long assignedCount;
    private Long completedCount;
    private Double avgProgress;
    /** completedCount / assignedCount * 100 */
    private Double completionRate;
}
