package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.cms.controller.CourseController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseDetailsResponseDTO;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.course.CourseRequestDto;
import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.course.CourseResponseWithItemDto;
import com.aspire.asat.cms.dto.course.CourseUpdateDto;
import com.aspire.asat.cms.model.Course;
import com.aspire.asat.cms.service.CourseService;
import com.aspire.asat.common.enums.ActivityType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CourseControllerImpl implements CourseController {

    private final CourseService courseService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created course: #{#courseDto.name != null ? #courseDto.name : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<CourseResponseDto>> createCourse(CourseRequestDto courseDto) {
        CourseResponseDto savedCourse = courseService.saveCourse(courseDto);
        ApiResponseDto<CourseResponseDto> response = new ApiResponseDto<>("Course created successfully", HttpStatus.CREATED.value(), savedCourse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CourseResponseDto>>>> getAllCourses(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        List<CourseResponseDto> courses = courseService.getAllCourses(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<CourseResponseDto>> allResponseDto = new AllResponseDto<>(offset, pageSize, courseService.getTotalCourseCount(), courses);
        ApiResponseDto<AllResponseDto<List<CourseResponseDto>>> response = new ApiResponseDto<>("Courses fetched successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CourseResponseDto>>>> getAllCoursesByStatus(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order) {
        List<CourseResponseDto> responseDtos = courseService.getAllCoursesByStatus(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<CourseResponseDto>> allResponseDto = new AllResponseDto<>(offset, pageSize, courseService.getTotalCourseCount(), responseDtos);
        ApiResponseDto<AllResponseDto<List<CourseResponseDto>>> response = new ApiResponseDto<>("Courses retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CourseResponseWithItemDto>> getCourseById(String CourseId) {
        CourseResponseWithItemDto course = courseService.getCourseById(CourseId);
        ApiResponseDto<CourseResponseWithItemDto> response = new ApiResponseDto<>("Course fetched successfully", HttpStatus.OK.value(), course);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated course: #{#CourseId}",
            oldValueExpression = "#{#CourseId}",
            newValueExpression = "#{#courseUpdateDto.name != null ? #courseUpdateDto.name : #CourseId}"
    )
    public ResponseEntity<ApiResponseDto<CourseResponseDto>> updateCourseById(String CourseId, CourseUpdateDto courseUpdateDto) {
        CourseResponseDto updatedCourse = courseService.updateCourseById(CourseId, courseUpdateDto);
        ApiResponseDto<CourseResponseDto> response = new ApiResponseDto<>("Course updated successfully", HttpStatus.OK.value(), updatedCourse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CourseResponseDto>> deleteCourseById(String CourseId) {
        CourseResponseDto deletedCourse = courseService.deleteCourseById(CourseId);
        ApiResponseDto<CourseResponseDto> response = new ApiResponseDto<>("Course deleted successfully", HttpStatus.OK.value(), deletedCourse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<Course> searchCourses(String query) {
        return courseService.searchWithRelevance(query);
    }

    @Override
    public void exportCoursesToCsv(Integer offset, Integer pageSize, HttpServletResponse response) {
        courseService.exportCoursesToCsv(offset, pageSize, response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> deleteCoursesByIds(@Valid @RequestBody ListOfUUID requestDto) {
        List<String> deletedCourseNames = courseService.deleteCoursesByIds(requestDto.getIds());
        ApiResponseDto<List<String>> response = new ApiResponseDto<>("Courses deleted successfully", HttpStatus.OK.value(), deletedCourseNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> bulkUpdateCoursesStatus(@RequestBody BulkStatusUpdateRequestDto requestDto) {
        List<String> updatedCourseNames = courseService.updateCoursesStatusByIds(
                requestDto.getIds(),
                requestDto.getStatus()
        );
        ApiResponseDto<List<String>> response = new ApiResponseDto<>(
                "Courses updated successfully", HttpStatus.OK.value(), updatedCourseNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportBulkCoursesToCsv(@RequestBody ListOfUUID requestDto, HttpServletResponse response) {
        courseService.exportBulkCourses(requestDto.getIds(), response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientCourseResponseDTO>>>> getCourseList(
            String userId, String packageId, String status, String search, Boolean isSaved, int offset, int pageSize) {

        log.info("Getting courses for user: {} with packageId: {}, status: {}, search: {}, isSaved: {}",
                userId, packageId, status, search, isSaved);
        List<ClientCourseResponseDTO> items = courseService.getUserCourses(userId, packageId, status, search, isSaved, offset, pageSize);
        long total = (packageId != null && !packageId.isBlank())
                ? courseService.countUserCoursesByPackage(packageId, status, search, isSaved)
                : courseService.countUserCourses(userId, null, status, search, isSaved);

        AllResponseDto<List<ClientCourseResponseDTO>> response = new AllResponseDto<>(offset, pageSize, total, items);
        return ResponseEntity.ok(new ApiResponseDto<>("Course list fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CourseDetailsResponseDTO>> getCourseDetails(String courseId, String userId, String packageId) {
        log.info("Getting course details for course: {}, user: {}, package: {}", courseId, userId, packageId);
        CourseDetailsResponseDTO response = courseService.getCourseDetails(courseId, userId, packageId);
        return ResponseEntity.ok(new ApiResponseDto<>("Course details fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> bookmarkCourse(String courseId, String userId, String packageId, boolean isSaved) {
        log.info("Bookmarking course: {} for user: {} in package: {} as saved: {}", courseId, userId, packageId, isSaved);
        courseService.bookmarkCourse(courseId, userId, packageId, isSaved);
        return ResponseEntity.ok(new ApiResponseDto<>("Bookmark status updated successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> assignCourseToUser(String courseId, String userId, String packageId) {
        log.info("Assigning course: {} to user: {}", courseId, userId);
        courseService.assignCourseToUser(userId, courseId);
        return ResponseEntity.ok(new ApiResponseDto<>("Course assigned to user successfully", 200, null));
    }
}
