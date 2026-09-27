package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.DashboardController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.dashboard.LicenseDistributionResponseDto;
import com.aspire.asat.registration.data.dashboard.UserStatusCountResponseDto;
import com.aspire.asat.registration.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementation for dashboard-related endpoints
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DashboardControllerImpl implements DashboardController {

    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<ApiResponseDto<UserStatusCountResponseDto>> getUserStatusCounts(String mspId) {
        log.info("Request received for user status counts, mspId={}", mspId);
        UserStatusCountResponseDto response = dashboardService.getUserStatusCounts(mspId);
        return ResponseEntity.ok(
                new ApiResponseDto<>("User status counts retrieved successfully", 200, response)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<LicenseDistributionResponseDto>> getLicenseDistribution(String mspId) {
        log.info("Request received for license distribution, mspId={}", mspId);
        LicenseDistributionResponseDto response = dashboardService.getLicenseDistribution(mspId);
        return ResponseEntity.ok(
                new ApiResponseDto<>("License distribution retrieved successfully", 200, response)
        );
    }
}
