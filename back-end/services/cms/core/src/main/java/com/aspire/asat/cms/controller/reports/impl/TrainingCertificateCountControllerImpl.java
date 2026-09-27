package com.aspire.asat.cms.controller.reports.impl;

import com.aspire.asat.cms.controller.reports.TrainingCertificateCountController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.reports.TrainingCertificateCountResponseDto;
import com.aspire.asat.cms.service.reports.TrainingCertificateCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TrainingCertificateCountControllerImpl implements TrainingCertificateCountController {

    private final TrainingCertificateCountService trainingCertificateCountService;

    @Override
    public ResponseEntity<ApiResponseDto<TrainingCertificateCountResponseDto>> getTrainingCertificateCounts(
            String clientAdminId) {
        try {
            TrainingCertificateCountResponseDto response =
                    trainingCertificateCountService.getTrainingCertificateCounts(clientAdminId);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Training and certificate counts retrieved successfully", 200, response));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid training/certificate count request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving training/certificate counts for clientAdminId={}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve training and certificate counts: " + e.getMessage(), 500, null));
        }
    }
}
