package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.LessonDto;
import com.aspire.asat.registration.exception.LessonDoesNotExistException;
import com.aspire.asat.registration.model.Lesson;
import com.aspire.asat.registration.repository.LessonRepository;
import com.aspire.asat.registration.service.LessonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {

    public static final String LESSON_IS_NULL = "Lesson is null";
    private final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Override
    public List<LessonDto> getAllLessons(Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Lesson> pageClient = lessonRepository.findAll(pageable);
        List<Lesson> allLesson = pageClient.getContent();
        return allLesson.stream().map(lesson -> lesson.toLessonDto(lesson)).collect(Collectors.toList());
    }

    @Override
    public LessonDto save(LessonDto saveLessonDto) {
        Lesson lesson = lessonRepository.save(Lesson.toLesson(saveLessonDto));
        return lesson.toLessonDto(lesson);
    }

    @Override
    public LessonDto getLessonById(UUID id) {
        Optional<Lesson> lessonFromDb = lessonRepository.findById(id);
        if (lessonFromDb.isEmpty()) {
            throw new LessonDoesNotExistException(LESSON_IS_NULL);
        }
        return Lesson.toLessonDto(lessonFromDb.get());
    }

    @Override
    public LessonDto updateLesson(LessonDto toBeUpdate) {
        if (!lessonRepository.existsById(toBeUpdate.getId())) {
            throw new LessonDoesNotExistException(LESSON_IS_NULL);
        }
        Lesson updatedLesson = lessonRepository.save(Lesson.toUpdateLesson(toBeUpdate));
        return Lesson.toLessonDto(updatedLesson);
    }

}
