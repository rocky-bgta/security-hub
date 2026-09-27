package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.SmsServerConfigurationController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.SmsServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.SmsServerTestRequest;
import com.aspire.asat.phishing.dto.response.SmsServerConfigurationDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.service.SmsServerConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SmsServerConfigurationControllerImpl implements SmsServerConfigurationController {

    private final SmsServerConfigurationService smsServerConfigurationService;

    @Override
    public ResponseEntity<AllResponseDto<List<SmsServerConfigurationDto>>> list(
            int offset, int pageSize, String clientId) {
        List<SmsServerConfigurationDto> items = smsServerConfigurationService.getConfigurations(offset, pageSize, clientId);
        long total = smsServerConfigurationService.countConfigurations(clientId);
        return ResponseEntity.ok(AllResponseDto.<List<SmsServerConfigurationDto>>builder()
                .items(items)
                .total(total)
                .offset(offset)
                .pageSize(pageSize)
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> getById(String id) {
        return ResponseEntity.ok(ApiResponseDto.<SmsServerConfigurationDto>builder()
                .data(smsServerConfigurationService.getById(id))
                .message("SMS server configuration retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> create(SmsServerConfigurationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.<SmsServerConfigurationDto>builder()
                        .data(smsServerConfigurationService.create(request))
                        .message("SMS server configuration created successfully")
                        .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> update(String id, SmsServerConfigurationRequest request) {
        return ResponseEntity.ok(ApiResponseDto.<SmsServerConfigurationDto>builder()
                .data(smsServerConfigurationService.update(id, request))
                .message("SMS server configuration updated successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> delete(String id) {
        smsServerConfigurationService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(id)
                .message("SMS server configuration deleted successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> getDefault() {
        return ResponseEntity.ok(ApiResponseDto.<SmsServerConfigurationDto>builder()
                .data(smsServerConfigurationService.getDefault())
                .message("Default SMS server configuration retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<SmsServerConfigurationDto>> setDefault(String id) {
        return ResponseEntity.ok(ApiResponseDto.<SmsServerConfigurationDto>builder()
                .data(smsServerConfigurationService.setDefault(id))
                .message("Default SMS server updated successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<TestResultDto>> test(String id, SmsServerTestRequest request) {
        return ResponseEntity.ok(ApiResponseDto.<TestResultDto>builder()
                .data(smsServerConfigurationService.testConfiguration(id, request))
                .message("SMS test completed")
                .build());
    }
}
