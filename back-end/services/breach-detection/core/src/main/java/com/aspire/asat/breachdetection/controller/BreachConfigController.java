package com.aspire.asat.breachdetection.controller;

import com.aspire.asat.breachdetection.constant.WebApiUrlConstants;
import com.aspire.asat.breachdetection.dto.ApiResponseDto;
import com.aspire.asat.breachdetection.dto.request.BreachConfigRequest;
import com.aspire.asat.breachdetection.dto.response.BreachConfigDto;
import com.aspire.asat.breachdetection.dto.response.BreachSyncResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "EMAIL Breach Detection Config", description = "APIs for managing breach detection configuration")
@RequestMapping(value = WebApiUrlConstants.INSECURE_WEB_API)
public interface BreachConfigController {

    @Operation(summary = "Get breach detection configuration")
    @GetMapping("/config")
    ResponseEntity<ApiResponseDto<BreachConfigDto>> getConfig();

    @Operation(summary = "Update breach detection configuration")
    @PostMapping("/config")
    ResponseEntity<ApiResponseDto<BreachConfigDto>> updateConfig(@RequestBody BreachConfigRequest request);

    @Operation(summary = "Trigger manual breach sync")
    @PostMapping("/sync")
    ResponseEntity<ApiResponseDto<BreachSyncResultDto>> triggerSync();
}
