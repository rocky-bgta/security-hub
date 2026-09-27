package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.LessonController;
import com.aspire.asat.registration.data.LessonDto;
import com.aspire.asat.registration.service.LessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class LessonControllerImpl implements LessonController {

    private final LessonService lessonService;

    public LessonControllerImpl(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @Override
    public ResponseEntity<LessonDto> createLesson(@RequestBody LessonDto saveLessonDto) {
        LessonDto savedLesson = lessonService.save(saveLessonDto);
        return new ResponseEntity<>(savedLesson, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<LessonDto>> getAllLesson(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize) {
        List<LessonDto> lessons = lessonService.getAllLessons(offset, pageSize);
        return new ResponseEntity<>(lessons, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LessonDto> getLessonById(@PathVariable("id") UUID id) {
        LessonDto lessonDto = lessonService.getLessonById(id);
        return new ResponseEntity<>(lessonDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LessonDto> updateLesson(@RequestBody LessonDto toBeUpdate) {
        LessonDto lessonDtoFromDb = lessonService.updateLesson(toBeUpdate);
        return new ResponseEntity<>(lessonDtoFromDb, HttpStatus.OK);
    }

}
