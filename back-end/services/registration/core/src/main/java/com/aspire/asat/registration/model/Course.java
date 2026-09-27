package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.CourseDto;
import com.aspire.asat.registration.data.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Course {

    @Id
    private UUID id;
    private String title;
    private CourseStatus status;
    private String description;

    public static CourseDto toCourseDto(Course course) {
        return CourseDto.builder().
                id(course.getId()).
                title(course.getTitle()).
                status(course.getStatus()).
                description(course.getDescription()).
                build();
    }

    public static Course toCourse(CourseDto courseDto) {
        return Course.builder().
                id(UUID.randomUUID()).
                title(courseDto.getTitle()).
                status(courseDto.getStatus()).
                description(courseDto.getDescription()).
                build();
    }

    public static Course toUpdateCourse(CourseDto courseDto) {
        return Course.builder().
                id(courseDto.getId()).
                title(courseDto.getTitle()).
                status(courseDto.getStatus()).
                description(courseDto.getDescription()).
                build();
    }

}
