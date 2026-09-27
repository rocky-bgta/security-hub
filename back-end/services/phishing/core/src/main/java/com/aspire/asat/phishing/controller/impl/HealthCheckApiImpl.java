package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.enums.ResponseMessage;
import com.aspire.asat.phishing.controller.HealthCheckApi;
import com.aspire.asat.phishing.controller.base.BaseController;
import com.aspire.asat.phishing.dto.apiResponses.ApiResponse;
import com.aspire.asat.phishing.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller implementation for phishing service
 */
@Slf4j
@RestController
public class HealthCheckApiImpl extends BaseController implements HealthCheckApi {

    @Override
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        log.info("Health check endpoint called");
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "phishing-service");
        return ResponseUtils.createSuccessResponseObject(getMessage(ResponseMessage.OPERATION_SUCCESSFUL), healthData);
    }
}

