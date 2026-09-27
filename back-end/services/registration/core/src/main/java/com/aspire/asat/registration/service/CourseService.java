package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.CourseDto;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    ApiResponse<List<CourseDto>> getAllCourses(Integer offset, Integer pageSize);

    CourseDto save(CourseDto saveCourseDto);

    CourseDto getCourseById(UUID id);

    CourseDto updateCourse(CourseDto toBeUpdate);

}
