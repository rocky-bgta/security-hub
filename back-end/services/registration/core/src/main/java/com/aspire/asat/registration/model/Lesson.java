package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.LessonDto;
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

public class Lesson {

    @Id
    private UUID id;
    private String name;
    private String logo;
    private String description;

    public static LessonDto toLessonDto(Lesson lesson) {
        return LessonDto.builder().
                id(lesson.getId()).
                name(lesson.getName()).
                logo(lesson.getLogo()).
                description(lesson.getDescription()).
                build();
    }

    public static Lesson toLesson(LessonDto lessonDto) {
        return Lesson.builder().
                id(UUID.randomUUID()).
                name(lessonDto.getName()).
                logo(lessonDto.getLogo()).
                description(lessonDto.getDescription()).
                build();
    }

    public static Lesson toUpdateLesson(LessonDto lessonDto) {
        return Lesson.builder().
                id(lessonDto.getId()).
                name(lessonDto.getName()).
                logo(lessonDto.getLogo()).
                description(lessonDto.getDescription()).
                build();
    }

}
