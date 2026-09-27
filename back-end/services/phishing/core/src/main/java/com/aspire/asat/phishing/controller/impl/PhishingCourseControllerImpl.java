package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.PhishingCourseController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.PhishingCourseDetailDto;
import com.aspire.asat.phishing.dto.response.PhishingCourseStatisticsDto;
import com.aspire.asat.phishing.service.PhishingCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PhishingCourseControllerImpl implements PhishingCourseController {

    private final PhishingCourseService phishingCourseService;

    @Override
    public ResponseEntity<ApiResponseDto<PhishingCourseStatisticsDto>> getStatistics(CampaignChannel channel) {
        try {
            PhishingCourseStatisticsDto data = phishingCourseService.getStatistics(channel);
            return ResponseEntity.ok(ApiResponseDto.<PhishingCourseStatisticsDto>builder()
                    .data(data)
                    .message("Phishing course statistics retrieved successfully")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e) {
            log.error("Error getting phishing course statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<PhishingCourseStatisticsDto>builder()
                            .message("Failed to retrieve phishing course statistics: " + e.getMessage())
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PhishingCourseDetailDto>>>> getDetails(
            int offset, int pageSize, String email) {
        try {
            AllResponseDto<List<PhishingCourseDetailDto>> data =
                    phishingCourseService.getDetails(offset, pageSize, email);
            return ResponseEntity.ok(ApiResponseDto.<AllResponseDto<List<PhishingCourseDetailDto>>>builder()
                    .data(data)
                    .message("Phishing course details retrieved successfully")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e) {
            log.error("Error getting phishing course details for offset={}, pageSize={}, email={}", offset, pageSize, email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AllResponseDto<List<PhishingCourseDetailDto>>>builder()
                            .message("Failed to retrieve phishing course details: " + e.getMessage())
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
