package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.apiResponses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Controller interface for phishing service
 */
@Tag(name = "Health Check", description = "Health Check for Phishing Service")
@RequestMapping(value = WebApiUrlConstants.PHISHING_API, produces = "application/json")
public interface HealthCheckApi {

    @GetMapping(value = WebApiUrlConstants.PHISHING_HEALTH_PATH)
    @Operation(summary = "Health check", description = "Check the health status of the phishing service")
    ResponseEntity<ApiResponse<Map<String, String>>> health();

}
