package com.aspire.asat.breachdetection.controller.impl;

import com.aspire.asat.breachdetection.controller.BreachFindingController;
import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.ApiResponseDto;
import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import com.aspire.asat.breachdetection.dto.request.InsecureWebBreachSearchRequest;
import com.aspire.asat.breachdetection.dto.response.BreachActivityResponseDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebBreachFindingDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebFindingsSummaryDto;
import com.aspire.asat.breachdetection.service.BreachDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BreachFindingControllerImpl implements BreachFindingController {

    private final BreachDetectionService breachDetectionService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InsecureWebBreachFindingDto>>>> getFindings(
            int offset, int pageSize, String sortBy, String sortDirection,
            String fromDate, String toDate, String email, String domain, InsecureWebBreachStatus breachStatus) {
        try {
            AllResponseDto<List<InsecureWebBreachFindingDto>> data =
                    breachDetectionService.getFindings(
                            offset, pageSize, sortBy, sortDirection, fromDate, toDate, email, domain, breachStatus);
            return ResponseEntity.ok(ApiResponseDto.<AllResponseDto<List<InsecureWebBreachFindingDto>>>builder()
                    .message("Findings retrieved successfully")
                    .data(data)
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach findings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AllResponseDto<List<InsecureWebBreachFindingDto>>>builder()
                            .message("Failed to retrieve findings: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<InsecureWebBreachFindingDto>>>> searchExternalBreaches(
            InsecureWebBreachSearchRequest request) {
        try {
            AllResponseDto<List<InsecureWebBreachFindingDto>> data = breachDetectionService.searchExternalBreaches(request);
            return ResponseEntity.ok(ApiResponseDto.<AllResponseDto<List<InsecureWebBreachFindingDto>>>builder()
                    .message("External breaches retrieved successfully")
                    .data(data)
                    .build());
        } catch (Exception e) {
            log.error("Error performing live InsecureWeb search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AllResponseDto<List<InsecureWebBreachFindingDto>>>builder()
                            .message("Failed to search breaches: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachActivityResponseDto>> getEmailBreachActivity(int days) {
        try {
            BreachActivityResponseDto data = breachDetectionService.getEmailBreachActivity(days);
            return ResponseEntity.ok(ApiResponseDto.<BreachActivityResponseDto>builder()
                    .message("Breach activity retrieved successfully")
                    .data(data)
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach activity for days={}", days, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<BreachActivityResponseDto>builder()
                            .message("Failed to retrieve breach activity: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<InsecureWebFindingsSummaryDto>> getFindingsSummary() {
        try {
            InsecureWebFindingsSummaryDto data = breachDetectionService.getFindingsSummary();
            return ResponseEntity.ok(ApiResponseDto.<InsecureWebFindingsSummaryDto>builder()
                    .message("Findings summary retrieved successfully")
                    .data(data)
                    .build());
        } catch (Exception e) {
            log.error("Error getting findings summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<InsecureWebFindingsSummaryDto>builder()
                            .message("Failed to retrieve findings summary: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<InsecureWebBreachFindingDto>> getFindingById(String id) {
        try {
            InsecureWebBreachFindingDto data = breachDetectionService.getFindingById(id);
            return ResponseEntity.ok(ApiResponseDto.<InsecureWebBreachFindingDto>builder()
                    .message("Finding retrieved successfully")
                    .data(data)
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach finding {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<InsecureWebBreachFindingDto>builder()
                            .message("Failed to retrieve finding: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<String>>>> getSources(
            int offset, int pageSize, String sortBy, String sortDirection) {
        try {
            AllResponseDto<List<String>> data = breachDetectionService.getSources(offset, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(ApiResponseDto.<AllResponseDto<List<String>>>builder()
                    .message("Sources retrieved successfully")
                    .data(data)
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach sources", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AllResponseDto<List<String>>>builder()
                            .message("Failed to retrieve sources: " + e.getMessage())
                            .build());
        }
    }
}
