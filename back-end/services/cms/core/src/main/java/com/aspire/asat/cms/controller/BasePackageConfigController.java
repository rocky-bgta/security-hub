package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigRequest;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigResponse;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Base Package Config Management", description = "APIs for managing base package configurations")
@RequestMapping(value = WebApiUrlConstants.BASE_PACKAGE_CONFIG_API, produces = "application/json")
public interface BasePackageConfigController {
    
    @Operation(summary = "Create base package config", description = "Create a new base package configuration")
    @PostMapping
    ResponseEntity<ApiResponseDto<BasePackageConfigResponse>> createBasePackageConfig(
            @Valid @RequestBody BasePackageConfigRequest request);
    
    @Operation(summary = "Get all base package configs", description = "Retrieve all base package configurations")
    @GetMapping
    ResponseEntity<ApiResponseDto<List<BasePackageConfigResponse>>> getAllBasePackageConfigs();
    
    @Operation(summary = "Get base package config by ID", description = "Retrieve a specific base package configuration by ID")
    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<BasePackageConfigResponse>> getBasePackageConfigById(@PathVariable String id);
    
    @Operation(summary = "Update base package config", description = "Update an existing base package configuration")
    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<BasePackageConfigResponse>> updateBasePackageConfig(
            @PathVariable String id, @Valid @RequestBody BasePackageConfigUpdateRequest request);
    
    @Operation(summary = "Delete base package config", description = "Soft delete a base package configuration")
    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<String>> deleteBasePackageConfig(@PathVariable String id);
    
    @Operation(summary = "Check if base package config name exists", description = "Check if a base package config with the given name exists")
    @GetMapping(WebApiUrlConstants.EXISTS)
    ResponseEntity<ApiResponseDto<Boolean>> existsByName(@RequestParam("name") String name);
}
