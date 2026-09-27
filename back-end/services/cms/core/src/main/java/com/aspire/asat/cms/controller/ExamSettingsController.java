package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsCreateRequestDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller interface for managing client exam settings
 */
@Tag(name = "Exam Settings", description = "APIs for managing client-specific exam settings")
@RequestMapping(value = WebApiUrlConstants.CLIENT_EXAM_SETTINGS_API, produces = "application/json")
public interface ExamSettingsController {

    @PostMapping
    @Operation(summary = "Create client exam settings", description = "Create new exam settings for a specific client")
    ResponseEntity<ApiResponseDto<ExamSettingsDto>> createSettings(
            @RequestBody ExamSettingsCreateRequestDto request);

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get settings by client ID", description = "Retrieve active exam settings for a specific client")
    ResponseEntity<ApiResponseDto<ExamSettingsDto>> getSettingsByClientId(
            @Parameter(description = "Client ID") @PathVariable String clientId);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get settings by ID", description = "Retrieve exam settings by their unique identifier")
    ResponseEntity<ApiResponseDto<ExamSettingsDto>> getSettingsById(
            @Parameter(description = "Settings ID") @PathVariable String id);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update settings", description = "Update existing exam settings")
    ResponseEntity<ApiResponseDto<ExamSettingsDto>> updateSettings(
            @Parameter(description = "Settings ID") @PathVariable String id,
            @RequestBody ExamSettingsUpdateRequestDto request);

    @GetMapping("/client/{clientId}/all")
    @Operation(summary = "Get all settings for client", description = "Retrieve all exam settings for a specific client")
    ResponseEntity<ApiResponseDto<List<ExamSettingsDto>>> getAllSettingsByClientId(
            @Parameter(description = "Client ID") @PathVariable String clientId);

    @GetMapping("/active")
    @Operation(summary = "Get all active settings", description = "Retrieve all active exam settings")
    ResponseEntity<ApiResponseDto<List<ExamSettingsDto>>> getAllActiveSettings();

    @GetMapping("/default")
    @Operation(summary = "Get default settings", description = "Retrieve default exam settings")
    ResponseEntity<ApiResponseDto<ExamSettingsDto>> getDefaultSettings();

    @GetMapping("/client/{clientId}/exists")
    @Operation(summary = "Check if settings exist", description = "Check if settings exist for a specific client")
    ResponseEntity<ApiResponseDto<Boolean>> settingsExistForClient(
            @Parameter(description = "Client ID") @PathVariable String clientId);
}
