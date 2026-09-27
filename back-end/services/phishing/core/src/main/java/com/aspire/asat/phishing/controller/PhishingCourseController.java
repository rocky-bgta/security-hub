package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.PhishingCourseDetailDto;
import com.aspire.asat.phishing.dto.response.PhishingCourseStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller interface for the Phishing Course dashboard endpoints.
 */
@Tag(name = "Phishing Course Dashboard", description = "APIs for phishing course statistics and details")
@RequestMapping(value = WebApiUrlConstants.PHISHING_COURSE_PATH)
public interface PhishingCourseController {

    @Operation(summary = "Get phishing course statistics",
            description = "Returns counts and percentages of phishing course enrollments (total, completed, in-progress, pending, expired) for the current client admin.")
    @GetMapping("/statistics")
    ResponseEntity<ApiResponseDto<PhishingCourseStatisticsDto>> getStatistics(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get paginated phishing course details",
            description = "Returns a paginated list of phishing course enrollments enriched with campaign name, user details, and progress for the current client admin.")
    @GetMapping("/details")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PhishingCourseDetailDto>>>> getDetails(
            @Parameter(description = "Page index, zero-based")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @Parameter(description = "Optional case-insensitive email substring filter")
            @RequestParam(required = false) String email);
}
