package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.SmsServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.SmsServerTestRequest;
import com.aspire.asat.phishing.dto.response.SmsServerConfigurationDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SMS Server Configurations", description = "APIs for managing SMS delivery servers")
@RequestMapping(value = WebApiUrlConstants.SMS_SERVER_CONFIGURATIONS_PATH)
public interface SmsServerConfigurationController {

    @Operation(summary = "List SMS server configurations")
    @GetMapping
    ResponseEntity<AllResponseDto<List<SmsServerConfigurationDto>>> list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String clientId);

    @Operation(summary = "Get SMS server configuration by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> getById(@PathVariable String id);

    @Operation(summary = "Create SMS server configuration")
    @PostMapping
    ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> create(
            @Valid @RequestBody SmsServerConfigurationRequest request);

    @Operation(summary = "Update SMS server configuration")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> update(
            @PathVariable String id,
            @Valid @RequestBody SmsServerConfigurationRequest request);

    @Operation(summary = "Delete SMS server configuration")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id);

    @Operation(summary = "Get default SMS server configuration")
    @GetMapping("/default")
    ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> getDefault();

    @Operation(summary = "Set SMS server as default")
    @PostMapping("/{id}/set-default")
    ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> setDefault(@PathVariable String id);

    @Operation(summary = "Send test SMS using configuration")
    @PostMapping("/{id}/test")
    ResponseEntity<ApiResponseDto<TestResultDto>> test(
            @PathVariable String id,
            @Valid @RequestBody SmsServerTestRequest request);
}
