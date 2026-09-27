package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.LicensedUserIdsInternalController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.service.LicensedUserIdsInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LicensedUserIdsInternalControllerImpl implements LicensedUserIdsInternalController {

    private final LicensedUserIdsInternalService licensedUserIdsInternalService;

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> getLicensedUserIds(
            String clientId, String productPackageId) {
        try {
            List<String> userIds = licensedUserIdsInternalService.getLicensedUserIds(clientId, productPackageId);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Licensed user IDs retrieved successfully", 200, userIds));
        } catch (PhishingValidationException e) {
            log.warn("Invalid licensed user IDs request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to retrieve licensed user IDs for clientId={}, productPackageId={}",
                    clientId, productPackageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve licensed user IDs", 500, null));
        }
    }
}
