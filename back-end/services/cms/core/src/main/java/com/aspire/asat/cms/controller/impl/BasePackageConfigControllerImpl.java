package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.BasePackageConfigController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigRequest;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigResponse;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigUpdateRequest;
import com.aspire.asat.cms.service.BasePackageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BasePackageConfigControllerImpl implements BasePackageConfigController {
    
    private final BasePackageConfigService basePackageConfigService;
    
    @Override
    public ResponseEntity<ApiResponseDto<BasePackageConfigResponse>> createBasePackageConfig(
            @Valid @RequestBody BasePackageConfigRequest request) {
        BasePackageConfigResponse response = basePackageConfigService.createBasePackageConfig(request);
        ApiResponseDto<BasePackageConfigResponse> apiResponse = new ApiResponseDto<>(
                "Base package config created successfully", 201, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<List<BasePackageConfigResponse>>> getAllBasePackageConfigs() {
        List<BasePackageConfigResponse> response = basePackageConfigService.getAllBasePackageConfigs();
        ApiResponseDto<List<BasePackageConfigResponse>> apiResponse = new ApiResponseDto<>(
                "Base package configs fetched successfully", 200, response);
        return ResponseEntity.ok(apiResponse);
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<BasePackageConfigResponse>> getBasePackageConfigById(@PathVariable String id) {
        BasePackageConfigResponse response = basePackageConfigService.getBasePackageConfigById(id);
        ApiResponseDto<BasePackageConfigResponse> apiResponse = new ApiResponseDto<>(
                "Base package config fetched successfully", 200, response);
        return ResponseEntity.ok(apiResponse);
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<BasePackageConfigResponse>> updateBasePackageConfig(
            @PathVariable String id, @Valid @RequestBody BasePackageConfigUpdateRequest request) {
        BasePackageConfigResponse response = basePackageConfigService.updateBasePackageConfig(id, request);
        ApiResponseDto<BasePackageConfigResponse> apiResponse = new ApiResponseDto<>(
                "Base package config updated successfully", 200, response);
        return ResponseEntity.ok(apiResponse);
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteBasePackageConfig(@PathVariable String id) {
        String response = basePackageConfigService.deleteBasePackageConfig(id);
        ApiResponseDto<String> apiResponse = new ApiResponseDto<>(
                "Base package config deleted successfully", 200, response);
        return ResponseEntity.ok(apiResponse);
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> existsByName(@RequestParam("name") String name) {
        Boolean exists = basePackageConfigService.existsByName(name);
        ApiResponseDto<Boolean> apiResponse = new ApiResponseDto<>(
                "Check completed successfully", 200, exists);
        return ResponseEntity.ok(apiResponse);
    }
}
