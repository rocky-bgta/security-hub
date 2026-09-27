package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseDetailsResponseDTO;
import com.aspire.asat.cms.dto.course.CourseRequestDto;
import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.course.CourseResponseWithItemDto;
import com.aspire.asat.cms.dto.course.CourseUpdateDto;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.model.Course;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface CourseService {
    CourseResponseDto saveCourse(CourseRequestDto courseDto);
    List<CourseResponseDto> getAllCourses(String search, Integer offset, Integer pageSize, String sortBy, String order);
    CourseResponseDto updateCourseById(String courseId, CourseUpdateDto courseUpdateDto);
    CourseResponseDto deleteCourseById(String courseId);
    CourseResponseWithItemDto getCourseById(String courseId);
    List<Course> searchWithRelevance(String text);
    void exportCoursesToCsv(Integer offset, Integer pageSize,  HttpServletResponse response);
    long getTotalCourseCount();
    List<String> deleteCoursesByIds(List<String> ids);
    List<String> updateCoursesStatusByIds(List<String> ids, Status status);
    void exportBulkCourses(List<String> ids, HttpServletResponse response);
    List<CourseResponseDto> getAllCoursesByStatus(String search, Integer offset, Integer pageSize, String sortBy, String order);

    // migrated methods

    void assignCourseToUser(String userId, String courseId);
    void bookmarkCourse(String courseId, String userId, String packageId, boolean isSaved);
    CourseDetailsResponseDTO getCourseDetails(String courseId, String userId, String packageId);
    List<ClientCourseResponseDTO> getUserCourses(String userId, String packageId, String status, String search, Boolean isSaved, int offset, int pageSize);

    long countUserCoursesByPackage(String packageId, String status, String search, Boolean isSaved);
    long countUserCourses(String userId, String packageId, String status, String search, Boolean isSaved);


}
