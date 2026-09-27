package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.VoiceServerConfigurationController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VoiceServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.VoiceServerTestRequest;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.dto.response.VoiceServerConfigurationDto;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoiceServerConfigurationControllerImpl implements VoiceServerConfigurationController {

    private final VoiceServerConfigurationService voiceServerConfigurationService;

    @Override
    public ResponseEntity<AllResponseDto<List<VoiceServerConfigurationDto>>> list(
            int offset, int pageSize, String clientId) {
        List<VoiceServerConfigurationDto> items = voiceServerConfigurationService.getConfigurations(offset, pageSize, clientId);
        long total = voiceServerConfigurationService.countConfigurations(clientId);
        return ResponseEntity.ok(AllResponseDto.<List<VoiceServerConfigurationDto>>builder()
                .items(items)
                .total(total)
                .offset(offset)
                .pageSize(pageSize)
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> getById(String id) {
        return ResponseEntity.ok(ApiResponseDto.<VoiceServerConfigurationDto>builder()
                .data(voiceServerConfigurationService.getById(id))
                .message("Voice server configuration retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> create(VoiceServerConfigurationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.<VoiceServerConfigurationDto>builder()
                        .data(voiceServerConfigurationService.create(request))
                        .message("Voice server configuration created successfully")
                        .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> update(String id, VoiceServerConfigurationRequest request) {
        return ResponseEntity.ok(ApiResponseDto.<VoiceServerConfigurationDto>builder()
                .data(voiceServerConfigurationService.update(id, request))
                .message("Voice server configuration updated successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> delete(String id) {
        voiceServerConfigurationService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(id)
                .message("Voice server configuration deleted successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> getDefault() {
        return ResponseEntity.ok(ApiResponseDto.<VoiceServerConfigurationDto>builder()
                .data(voiceServerConfigurationService.getDefault())
                .message("Default voice server configuration retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VoiceServerConfigurationDto>> setDefault(String id) {
        return ResponseEntity.ok(ApiResponseDto.<VoiceServerConfigurationDto>builder()
                .data(voiceServerConfigurationService.setDefault(id))
                .message("Default voice server updated successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<TestResultDto>> test(String id, VoiceServerTestRequest request) {
        return ResponseEntity.ok(ApiResponseDto.<TestResultDto>builder()
                .data(voiceServerConfigurationService.testConfiguration(id, request))
                .message("Voice test completed")
                .build());
    }
}
