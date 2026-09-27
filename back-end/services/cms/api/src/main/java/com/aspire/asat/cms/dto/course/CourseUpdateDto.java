package com.aspire.asat.cms.dto.course;

import com.aspire.asat.cms.dto.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CourseUpdateDto {

    @NotBlank(message = "Course name is required")
    private String courseName;

    private String courseDescription;

    @NotNull(message = "Course status is required")
    private CourseStatus courseStatus;

    @NotEmpty(message = "At least one chapter must be associated with the course")
    private List<String> chapterIds;

    @NotEmpty(message = "At least one product must be associated with the course")
    private List<String> productIds;
    private int totalContentCount;
    private String thumbnailUrl;
}
