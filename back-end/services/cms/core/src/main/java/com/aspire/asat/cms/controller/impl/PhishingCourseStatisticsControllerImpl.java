package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.PhishingCourseStatisticsController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;
import com.aspire.asat.cms.service.PhishingCourseStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PhishingCourseStatisticsControllerImpl implements PhishingCourseStatisticsController {

    private final PhishingCourseStatisticsService phishingCourseStatisticsService;

    @Override
    public ResponseEntity<ApiResponseDto<PhishingCourseStatisticsCountsDto>> getStatistics(
            String clientAdminId, List<String> subPackageIds) {
        log.info("Received phishing course statistics request for clientAdminId={}, subPackageIdsCount={}",
                clientAdminId, subPackageIds == null ? 0 : subPackageIds.size());
        PhishingCourseStatisticsCountsDto data =
                phishingCourseStatisticsService.getStatistics(clientAdminId, subPackageIds);
        return ResponseEntity.ok(new ApiResponseDto<>("Phishing course statistics retrieved successfully", 200, data));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PhishingCourseEnrollmentDto>>>> getDetails(
            String clientAdminId, int offset, int pageSize, List<String> userIds) {
        int userIdCount = userIds == null ? 0 : userIds.size();
        log.info("Received phishing course details request for clientAdminId={}, offset={}, pageSize={}, userIdsCount={}",
                clientAdminId, offset, pageSize, userIdCount);
        AllResponseDto<List<PhishingCourseEnrollmentDto>> data =
                phishingCourseStatisticsService.getDetails(clientAdminId, offset, pageSize, userIds);
        return ResponseEntity.ok(new ApiResponseDto<>("Phishing course details retrieved successfully", 200, data));
    }
}
