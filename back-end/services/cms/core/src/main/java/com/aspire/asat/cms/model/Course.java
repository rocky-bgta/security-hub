package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.course.CourseRequestDto;
import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.course.CourseResponseWithItemDto;
import com.aspire.asat.cms.dto.course.CourseUpdateDto;
import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.product.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private String id;
    private String courseName;
    private String courseDescription;
    private CourseStatus courseStatus;
    private List<String> chapterIds;
    private List<String> productIds;
    private String thumbnailUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer totalContentCount;


    public static CourseResponseDto toCourseDto(Course course) {
        return CourseResponseDto.builder().
                id(course.getId()).
                courseName(course.getCourseName()).
                courseDescription(course.getCourseDescription()).
                courseStatus(course.getCourseStatus()).
                chapterIds(course.getChapterIds()).
                productIds(course.getProductIds()).
                thumbnailUrl(course.getThumbnailUrl()).
                createdAt(course.getCreatedAt()).
                updatedAt(course.getUpdatedAt()).
                totalContentCount(course.getTotalContentCount() != null ? course.getTotalContentCount() : 0).
                build();
    }


    public static CourseResponseWithItemDto toCourseDtoWithItemDetails(Course course, List<ChapterResponseDto> chapterDetailDto, List<ResponseDto> productDetailDto) {
        return CourseResponseWithItemDto.builder().
                id(course.getId()).
                courseName(course.getCourseName()).
                courseDescription(course.getCourseDescription()).
                courseStatus(course.getCourseStatus()).
                chapterIds(chapterDetailDto).
                productIds(productDetailDto).
                thumbnailUrl(course.getThumbnailUrl()).
                createdAt(course.getCreatedAt()).
                updatedAt(course.getUpdatedAt()).
                totalContentCount(course.getTotalContentCount()).
                build();
    }

    public static Course toCourse(String id, CourseRequestDto dto, Instant createdAt, Instant updatedAt, int totalContentCount) {
        return Course.builder()
                .id(id)
                .courseName(dto.getCourseName())
                .courseDescription(dto.getCourseDescription())
                .chapterIds(dto.getChapterIds())
                .productIds(dto.getProductIds())
                .courseStatus(dto.getCourseStatus())
                .totalContentCount(totalContentCount)
                .thumbnailUrl(dto.getThumbnailUrl())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Course toUpdateCourse(CourseUpdateDto courseUpdateDto, String courseId, Instant updatedAt) {
        return Course.builder().
                id(courseId).
                courseName(courseUpdateDto.getCourseName()).
                courseDescription(courseUpdateDto.getCourseDescription()).
                courseStatus(courseUpdateDto.getCourseStatus()).
                chapterIds(courseUpdateDto.getChapterIds()).
                productIds(courseUpdateDto.getProductIds()).
                totalContentCount(courseUpdateDto.getTotalContentCount()).
                thumbnailUrl(courseUpdateDto.getThumbnailUrl()).
                updatedAt(updatedAt).
                build();
    }

}
