package com.aspire.asat.cms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicAssignmentSummaryDTO {
    private Long totalTopics;
    private Long assignedTopics;
    private Long unassignedTopics;
    /** Count of user_topics assignment rows in scope. */
    private Long assignedToUsers;
    /** Count of assignment rows with status COMPLETED (not distinct users). */
    private Long completedAssignments;
    private Double avgProgress;
}
