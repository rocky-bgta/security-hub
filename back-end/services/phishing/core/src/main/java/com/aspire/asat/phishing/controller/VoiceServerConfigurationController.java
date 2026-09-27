package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VoiceServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.VoiceServerTestRequest;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.dto.response.VoiceServerConfigurationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Voice Server Configurations", description = "APIs for managing voice delivery servers")
@RequestMapping(value = WebApiUrlConstants.VOICE_SERVER_CONFIGURATIONS_PATH)
public interface VoiceServerConfigurationController {

    @Operation(summary = "List voice server configurations")
    @GetMapping
    ResponseEntity<AllResponseDto<List<VoiceServerConfigurationDto>>> list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String clientId);

    @Operation(summary = "Get voice server configuration by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> getById(@PathVariable String id);

    @Operation(summary = "Create voice server configuration")
    @PostMapping
    ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> create(
            @Valid @RequestBody VoiceServerConfigurationRequest request);

    @Operation(summary = "Update voice server configuration")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> update(
            @PathVariable String id,
            @Valid @RequestBody VoiceServerConfigurationRequest request);

    @Operation(summary = "Delete voice server configuration")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id);

    @Operation(summary = "Get default voice server configuration")
    @GetMapping("/default")
    ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> getDefault();

    @Operation(summary = "Set voice server as default")
    @PostMapping("/{id}/set-default")
    ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> setDefault(@PathVariable String id);

    @Operation(summary = "Initiate test call using configuration")
    @PostMapping("/{id}/test")
    ResponseEntity<ApiResponseDto<TestResultDto>> test(
            @PathVariable String id,
            @Valid @RequestBody VoiceServerTestRequest request);
}
