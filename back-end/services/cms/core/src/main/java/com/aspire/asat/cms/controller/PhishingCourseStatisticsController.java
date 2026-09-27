package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Phishing Course Statistics", description = "Internal endpoints for phishing course dashboard data")
@RequestMapping(value = WebApiUrlConstants.PHISHING_COURSE_PATH, produces = "application/json")
public interface PhishingCourseStatisticsController {

    @Operation(summary = "Get phishing course statistics counts",
            description = "Returns raw enrollment counts (completed, in-progress, pending, expired) for phishing courses under the given client admin.")
    @GetMapping("/statistics")
    ResponseEntity<ApiResponseDto<PhishingCourseStatisticsCountsDto>> getStatistics(
            @Parameter(description = "Client admin ID", required = true)
            @RequestParam @NotBlank String clientAdminId,
            @Parameter(description = "Optional list of training sub-package IDs to restrict the count")
            @RequestParam(required = false) List<String> subPackageIds
    );

    @Operation(summary = "Get paginated phishing course enrollment details",
            description = "Returns paginated enrollment rows for phishing courses under the given client admin, optionally filtered by a set of user IDs (callers resolve email or other user-side filters to IDs first).")
    @GetMapping("/details")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PhishingCourseEnrollmentDto>>>> getDetails(
            @Parameter(description = "Client admin ID", required = true)
            @RequestParam @NotBlank String clientAdminId,
            @Parameter(description = "Page index, zero-based")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @Parameter(description = "Optional list of user IDs to filter enrollments by")
            @RequestParam(required = false) List<String> userIds);
}
