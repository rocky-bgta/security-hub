package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.LessonDto;

import java.util.List;
import java.util.UUID;

public interface LessonService {

    List<LessonDto> getAllLessons(Integer offset, Integer pageSize);

    LessonDto save(LessonDto saveLessonDto);

    LessonDto getLessonById(UUID id);

    LessonDto updateLesson(LessonDto toBeUpdate);

}
