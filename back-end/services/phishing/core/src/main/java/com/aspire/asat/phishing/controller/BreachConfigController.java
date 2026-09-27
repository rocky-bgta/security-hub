package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.BreachConfigRequest;
import com.aspire.asat.phishing.dto.response.BreachConfigDto;
import com.aspire.asat.phishing.dto.response.BreachSyncResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller interface for breach detection configuration endpoints.
 */
@Tag(name = "Breach Detection Config", description = "APIs for managing breach detection configuration")
@RequestMapping(value = WebApiUrlConstants.BREACH_DETECTION_PATH)
public interface BreachConfigController {

    @Operation(summary = "Get breach detection configuration")
    @GetMapping("/config")
    ResponseEntity<ApiResponseDto<BreachConfigDto>> getConfig();

    @Operation(summary = "Update breach detection configuration")
    @PostMapping("/config")
    ResponseEntity<ApiResponseDto<BreachConfigDto>> updateConfig(
            @RequestBody BreachConfigRequest request
    );

    @Operation(summary = "Trigger manual breach sync")
    @PostMapping("/sync")
    ResponseEntity<ApiResponseDto<BreachSyncResultDto>> triggerSync();
}
