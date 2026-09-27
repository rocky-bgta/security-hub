package com.aspire.asat.cms.dto.course;

import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.product.ResponseDto;
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
public class CourseResponseWithItemDto {
    private String id;
    private String courseName;
    private String courseDescription;
    private CourseStatus courseStatus;
    private List<ChapterResponseDto> chapterIds;
    private List<ResponseDto> productIds;
    private int totalContentCount;
    private String thumbnailUrl;
    Instant createdAt;
    Instant updatedAt;
}
