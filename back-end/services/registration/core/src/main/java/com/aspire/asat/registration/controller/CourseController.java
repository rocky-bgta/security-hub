package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.CourseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.COURSE_API, produces = "application/json")
public interface CourseController {

    @PostMapping
    ResponseEntity<CourseDto> createCourse(CourseDto saveCourseDto);

    @GetMapping
    ResponseEntity<ApiResponse<List<CourseDto>>> getAllCourses(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<CourseDto> getCourseById(UUID id);

    @PutMapping
    ResponseEntity<CourseDto> updateCourse(@RequestBody CourseDto toBeUpdate);

}
