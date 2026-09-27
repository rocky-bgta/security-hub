package com.aspire.asat.breachdetection.controller.impl;

import com.aspire.asat.breachdetection.controller.BreachConfigController;
import com.aspire.asat.breachdetection.dto.ApiResponseDto;
import com.aspire.asat.breachdetection.dto.request.BreachConfigRequest;
import com.aspire.asat.breachdetection.dto.response.BreachConfigDto;
import com.aspire.asat.breachdetection.dto.response.BreachSyncResultDto;
import com.aspire.asat.breachdetection.service.BreachDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BreachConfigControllerImpl implements BreachConfigController {

    private final BreachDetectionService breachDetectionService;

    @Override
    public ResponseEntity<ApiResponseDto<BreachConfigDto>> getConfig() {
        try {
            BreachConfigDto config = breachDetectionService.getConfig();
            return ResponseEntity.ok(ApiResponseDto.<BreachConfigDto>builder()
                    .data(config)
                    .message("Configuration retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<BreachConfigDto>builder()
                            .message("Failed to retrieve configuration: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachConfigDto>> updateConfig(BreachConfigRequest request) {
        try {
            BreachConfigDto config = breachDetectionService.updateConfig(request);
            return ResponseEntity.ok(ApiResponseDto.<BreachConfigDto>builder()
                    .data(config)
                    .message("Configuration updated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error updating breach config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<BreachConfigDto>builder()
                            .message("Failed to update configuration: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachSyncResultDto>> triggerSync() {
        try {
            BreachSyncResultDto result = breachDetectionService.triggerManualSync();
            return ResponseEntity.ok(ApiResponseDto.<BreachSyncResultDto>builder()
                    .data(result)
                    .message("Sync " + result.getStatus().name().toLowerCase())
                    .build());
        } catch (Exception e) {
            log.error("Error triggering breach sync", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<BreachSyncResultDto>builder()
                            .message("Sync failed: " + e.getMessage())
                            .build());
        }
    }
}
