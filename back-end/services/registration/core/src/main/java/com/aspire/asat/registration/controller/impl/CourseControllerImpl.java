package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.CourseController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.CourseDto;
import com.aspire.asat.registration.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class CourseControllerImpl implements CourseController {

    private final CourseService courseService;

    public CourseControllerImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto saveCourseDto) {
        CourseDto savedCourse = courseService.save(saveCourseDto);
        return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponse<List<CourseDto>>> getAllCourses(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize) {
        ApiResponse<List<CourseDto>> course = courseService.getAllCourses(offset, pageSize);
        System.out.println(courseService.getAllCourses(offset, pageSize));
        return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CourseDto> getCourseById(@PathVariable("id") UUID id) {
        CourseDto courseDto = courseService.getCourseById(id);
        return new ResponseEntity<>(courseDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CourseDto> updateCourse(@RequestBody CourseDto toBeUpdate) {
        CourseDto courseDtoFromDb = courseService.updateCourse(toBeUpdate);
        return new ResponseEntity<>(courseDtoFromDb, HttpStatus.OK);
    }

}
