package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.BreachConfigController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.BreachConfigRequest;
import com.aspire.asat.phishing.dto.response.BreachConfigDto;
import com.aspire.asat.phishing.dto.response.BreachSyncResultDto;
import com.aspire.asat.phishing.service.BreachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementation for breach detection configuration endpoints.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class BreachConfigControllerImpl implements BreachConfigController {

    private final BreachService breachService;

    @Override
    public ResponseEntity<ApiResponseDto<BreachConfigDto>> getConfig() {
        try {
            BreachConfigDto config = breachService.getConfig();
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
            BreachConfigDto config = breachService.updateConfig(request);
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
            log.info("Manual breach sync triggered");
            BreachSyncResultDto result = breachService.triggerManualSync();
            return ResponseEntity.ok(ApiResponseDto.<BreachSyncResultDto>builder()
                    .data(result)
                    .message("Sync " + result.getStatus().toLowerCase())
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
