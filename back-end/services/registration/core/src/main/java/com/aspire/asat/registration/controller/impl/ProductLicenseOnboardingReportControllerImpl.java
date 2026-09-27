package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.ProductLicenseOnboardingReportController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.ProductLicenseOnboardingReportDto;
import com.aspire.asat.registration.service.ProductLicenseOnboardingReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductLicenseOnboardingReportControllerImpl implements ProductLicenseOnboardingReportController {

    private final ProductLicenseOnboardingReportService productLicenseOnboardingReportService;

    @Override
    public ResponseEntity<ApiResponseDto<ProductLicenseOnboardingReportDto>> getProductLicenseOnboardingReport(
            String clientAdminId) {
        try {
            ProductLicenseOnboardingReportDto report =
                    productLicenseOnboardingReportService.getReport(clientAdminId);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Product license onboarding report generated successfully", 200, report));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid product license onboarding report request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error generating product license onboarding report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to generate product license onboarding report: " + e.getMessage(), 500, null));
        }
    }
}
