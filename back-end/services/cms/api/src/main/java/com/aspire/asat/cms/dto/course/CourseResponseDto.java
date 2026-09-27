package com.aspire.asat.cms.dto.course;

import com.aspire.asat.cms.dto.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto {
    private String id;
    private String courseName;
    private String courseDescription;
    private CourseStatus courseStatus;
    private List<String> chapterIds;
    private List<String> productIds;
    private int totalContentCount;
    private String thumbnailUrl;
    Instant createdAt;
    Instant updatedAt;
}
