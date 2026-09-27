package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.LessonDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.LESSON_API, produces = "application/json")
public interface LessonController {

    @PostMapping
    ResponseEntity<LessonDto> createLesson(LessonDto saveLessonDto);

    @GetMapping
    ResponseEntity<List<LessonDto>> getAllLesson(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<LessonDto> getLessonById(UUID id);

    @PutMapping
    ResponseEntity<LessonDto> updateLesson(@RequestBody LessonDto toBeUpdate);

}