package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.RequiredInfoController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.requiredinfo.RequiredInfoResponseDTO;
import com.aspire.asat.registration.service.RequiredInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementation for required info endpoints
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RequiredInfoControllerImpl implements RequiredInfoController {

    private final RequiredInfoService requiredInfoService;

    @Override
    public ResponseEntity<ApiResponseDto<RequiredInfoResponseDTO>> getRequiredInfo() {
        try {
            log.info("Getting required info for logged-in user");
            RequiredInfoResponseDTO response = requiredInfoService.getRequiredInfo();
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Required info retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting required info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve required info", 500, null));
        }
    }
}

