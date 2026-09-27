package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseWithProgressDTO {
    private String courseId;
    private String courseName;
    private Instant createdAt;
    private double progress;
}

