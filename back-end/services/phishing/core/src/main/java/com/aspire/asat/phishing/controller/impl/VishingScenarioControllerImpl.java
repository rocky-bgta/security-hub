package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.VishingScenarioController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VishingScenarioCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingScenarioUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingScenarioDto;
import com.aspire.asat.phishing.service.VishingScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VishingScenarioControllerImpl implements VishingScenarioController {

    private final VishingScenarioService vishingScenarioService;

    @Override
    public ResponseEntity<AllResponseDto<List<VishingScenarioDto>>> list(int offset, int pageSize, String searchParam) {
        List<VishingScenarioDto> items = vishingScenarioService.getScenarios(offset, pageSize, searchParam);
        long total = vishingScenarioService.countScenarios(searchParam);
        return ResponseEntity.ok(AllResponseDto.<List<VishingScenarioDto>>builder()
                .items(items)
                .total(total)
                .offset(offset)
                .pageSize(pageSize)
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingScenarioDto>> getById(String id) {
        return ResponseEntity.ok(ApiResponseDto.<VishingScenarioDto>builder()
                .data(vishingScenarioService.getById(id))
                .message("Vishing scenario retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingScenarioDto>> create(VishingScenarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.<VishingScenarioDto>builder()
                        .data(vishingScenarioService.create(request))
                        .message("Vishing scenario created successfully")
                        .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingScenarioDto>> update(String id, VishingScenarioUpdateRequest request) {
        return ResponseEntity.ok(ApiResponseDto.<VishingScenarioDto>builder()
                .data(vishingScenarioService.update(id, request))
                .message("Vishing scenario updated successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> delete(String id) {
        vishingScenarioService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(id)
                .message("Vishing scenario deleted successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingScenarioDto>> publish(String id) {
        return ResponseEntity.ok(ApiResponseDto.<VishingScenarioDto>builder()
                .data(vishingScenarioService.publish(id))
                .message("Vishing scenario published successfully")
                .build());
    }
}
