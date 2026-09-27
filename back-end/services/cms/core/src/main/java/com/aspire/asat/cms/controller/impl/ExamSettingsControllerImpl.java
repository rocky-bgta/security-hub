package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.ExamSettingsController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsCreateRequestDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsUpdateRequestDto;
import com.aspire.asat.cms.service.exam.ExamSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ExamSettingsControllerImpl implements ExamSettingsController {
    private final ExamSettingsService examSettingsService;

    @Override
    public ResponseEntity<ApiResponseDto<ExamSettingsDto>> createSettings(ExamSettingsCreateRequestDto request) {
        log.info("Creating client exam settings for client ID: {}", request.getClientId());
        ExamSettingsDto createdSettings = examSettingsService.createSettings(request);
        return new ResponseEntity<>(new ApiResponseDto<>("Settings created successfully", HttpStatus.CREATED.value(), createdSettings), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamSettingsDto>> getSettingsByClientId(String clientId) {
        log.info("Retrieving client exam settings for client ID: {}", clientId);
        ExamSettingsDto settings = examSettingsService.getSettingsByClientId(clientId);
        return new ResponseEntity<>(new ApiResponseDto<>("Settings retrieved successfully", HttpStatus.OK.value(), settings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamSettingsDto>> getSettingsById(String id) {
        log.info("Retrieving client exam settings by ID: {}", id);
        ExamSettingsDto settings = examSettingsService.getSettingsById(id);
        return new ResponseEntity<>(new ApiResponseDto<>("Settings retrieved successfully", HttpStatus.OK.value(), settings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamSettingsDto>> updateSettings(String id, ExamSettingsUpdateRequestDto request) {
        log.info("Updating client exam settings with ID: {}", id);
        ExamSettingsDto updatedSettings = examSettingsService.updateSettings(id, request);
        return new ResponseEntity<>(new ApiResponseDto<>("Settings updated successfully", HttpStatus.OK.value(), updatedSettings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ExamSettingsDto>>> getAllSettingsByClientId(String clientId) {
        log.info("Retrieving all client exam settings for client ID: {}", clientId);
        List<ExamSettingsDto> settings = examSettingsService.getAllSettingsByClientId(clientId);
        return new ResponseEntity<>(new ApiResponseDto<>("Settings retrieved successfully", HttpStatus.OK.value(), settings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ExamSettingsDto>>> getAllActiveSettings() {
        log.info("Retrieving all active client exam settings");
        List<ExamSettingsDto> settings = examSettingsService.getAllActiveSettings();
        return new ResponseEntity<>(new ApiResponseDto<>("Settings retrieved successfully", HttpStatus.OK.value(), settings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamSettingsDto>> getDefaultSettings() {
        log.info("Retrieving default client exam settings");
        ExamSettingsDto settings = examSettingsService.getDefaultSettings();
        return new ResponseEntity<>(new ApiResponseDto<>("Default settings retrieved successfully", HttpStatus.OK.value(), settings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> settingsExistForClient(String clientId) {
        log.info("Checking if settings exist for client ID: {}", clientId);
        boolean exists = examSettingsService.settingsExistForClient(clientId);
        return new ResponseEntity<>(new ApiResponseDto<>("Settings existence checked successfully", HttpStatus.OK.value(), exists), HttpStatus.OK);
    }
}
