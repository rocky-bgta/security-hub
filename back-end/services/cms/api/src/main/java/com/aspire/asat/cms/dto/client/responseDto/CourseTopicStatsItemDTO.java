package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTopicStatsItemDTO {
    private String courseId;
    private String courseName;
    private int total;
    private int completed;
    private int pending;
}
