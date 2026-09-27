package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.CourseDto;
import com.aspire.asat.registration.exception.CourseDoesNotExistException;
import com.aspire.asat.registration.model.Course;
import com.aspire.asat.registration.repository.CourseRepository;
import com.aspire.asat.registration.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    public static final String COURSE_IS_NULL = "Course is null";
    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public ApiResponse<List<CourseDto>> getAllCourses(Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Course> pageCourse = courseRepository.findAll(pageable);
        List<Course> allCourse = pageCourse.getContent();
        List<CourseDto> courseDtos = allCourse.stream().map(course -> course.toCourseDto(course)).collect(Collectors.toList());
        return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), courseDtos);
    }

    @Override
    public CourseDto save(CourseDto saveCourseDto) {
        Course course = courseRepository.save(Course.toCourse(saveCourseDto));
        return course.toCourseDto(course);
    }

    @Override
    public CourseDto getCourseById(UUID id) {
        Optional<Course> courseFromDb = courseRepository.findById(id);
        if (courseFromDb.isEmpty()) {
            throw new CourseDoesNotExistException(COURSE_IS_NULL);
        }
        return Course.toCourseDto(courseFromDb.get());
    }

    @Override
    public CourseDto updateCourse(CourseDto toBeUpdate) {
        if (!courseRepository.existsById(toBeUpdate.getId())) {
            throw new CourseDoesNotExistException(COURSE_IS_NULL);
        }
        Course updatedCourse = courseRepository.save(Course.toUpdateCourse(toBeUpdate));
        return Course.toCourseDto(updatedCourse);
    }

}
