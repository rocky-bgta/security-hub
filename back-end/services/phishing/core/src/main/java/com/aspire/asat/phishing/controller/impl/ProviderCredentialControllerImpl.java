package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.ProviderCredentialController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import com.aspire.asat.phishing.service.ProviderCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link ProviderCredentialController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ProviderCredentialControllerImpl implements ProviderCredentialController {

    private final ProviderCredentialService providerCredentialService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ProviderCredentialDto>>>> getProviderCredentials(
            String providerName, Boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        Page<ProviderCredentialDto> page = providerCredentialService.getProviderCredentials(
                providerName, isActive, offset, pageSize, sortBy, sortOrder);
        AllResponseDto<List<ProviderCredentialDto>> data =
                new AllResponseDto<>(offset, pageSize, page.getTotalElements(), page.getContent());
        return ResponseEntity.ok(new ApiResponseDto<>("Provider credentials retrieved successfully", 200, data));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProviderCredentialDto>> getProviderCredentialById(String id) {
        ProviderCredentialDto dto = providerCredentialService.getProviderCredentialById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Provider credential retrieved successfully", 200, dto));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProviderCredentialDto>> createProviderCredential(
            ProviderCredentialCreateRequest request) {
        ProviderCredentialDto created = providerCredentialService.createProviderCredential(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Provider credential created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProviderCredentialDto>> updateProviderCredential(
            String id, ProviderCredentialUpdateRequest request) {
        ProviderCredentialDto updated = providerCredentialService.updateProviderCredential(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Provider credential updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteProviderCredential(String id) {
        providerCredentialService.deleteProviderCredential(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Provider credential deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProviderCredentialDto>> setDefault(String id) {
        ProviderCredentialDto updated = providerCredentialService.setDefault(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Default provider updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProviderCredentialDto>> setActive(String id, boolean active) {
        ProviderCredentialDto updated = providerCredentialService.setActive(id, active);
        return ResponseEntity.ok(new ApiResponseDto<>("Provider status updated successfully", 200, updated));
    }
}
