package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.COURSE_API, produces = "application/json")
public interface CourseController {

    @PostMapping
    ResponseEntity<ApiResponseDto<CourseResponseDto>> createCourse(@RequestBody CourseRequestDto courseDto);

    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CourseResponseDto>>>> getAllCourses(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ENABLED)
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CourseResponseDto>>>> getAllCoursesByStatus(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<CourseResponseWithItemDto>> getCourseById(@PathVariable("id") String CourseId);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<CourseResponseDto>> updateCourseById(@PathVariable("id") String CourseId, @RequestBody CourseUpdateDto courseUpdateDto);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<CourseResponseDto>> deleteCourseById(@PathVariable("id") String CourseId);

    @GetMapping(WebApiUrlConstants.PATH_VAR_SEARCH)
    List<Course> searchCourses(@RequestParam String query);

    @GetMapping(value = "/export", produces = "text/csv")
    void exportCoursesToCsv(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "1000", required = false) Integer pageSize,
            HttpServletResponse response);


    @DeleteMapping(WebApiUrlConstants.PATH_VAR_BULK_DELETE)
    ResponseEntity<ApiResponseDto<List<String>>> deleteCoursesByIds(@Valid @RequestBody ListOfUUID requestDto);

    @PutMapping(WebApiUrlConstants.PATH_VAR_BULK_UPDATE)
    ResponseEntity<ApiResponseDto<List<String>>> bulkUpdateCoursesStatus(@RequestBody BulkStatusUpdateRequestDto requestDto);

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_BULK_EXPORT, produces = "text/csv")
    void exportBulkCoursesToCsv(@Valid @RequestBody ListOfUUID requestDto, HttpServletResponse response);

    // ========== CLIENT-FACING COURSE APIs ==========

    @Operation(summary = "Get user course list", description = "Returns all courses enrolled by the user with progress and certificate info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Courses listed"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/courses")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientCourseResponseDTO>>>> getCourseList(
            @RequestParam @NotBlank String userId,
            @RequestParam(required = false) String packageId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSaved,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get course details", description = "Returns course metadata and which contents are completed by the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course details fetched"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/courses/{courseId}")
    ResponseEntity<ApiResponseDto<CourseDetailsResponseDTO>> getCourseDetails(
            @PathVariable("courseId") @NotBlank String courseId,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String packageId);

    @Operation(summary = "Save or bookmark a course", description = "Marks a course as saved/bookmarked by the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course bookmarked or updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User or course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/client/courses/bookmark")
    ResponseEntity<ApiResponseDto<Void>> bookmarkCourse(
            @RequestParam @NotBlank String courseId,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String packageId,
            @RequestParam boolean isSaved);

    @Operation(summary = "Assign course to user", description = "Assigns a course to a user for enrollment or access")
    @PostMapping("/client/courses/assign")
    ResponseEntity<ApiResponseDto<Void>> assignCourseToUser(
            @RequestParam @NotBlank String courseId,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String packageId
    );
}
