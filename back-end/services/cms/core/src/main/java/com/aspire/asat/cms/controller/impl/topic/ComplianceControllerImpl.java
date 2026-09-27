package com.aspire.asat.cms.controller.impl.topic;

import com.aspire.asat.cms.controller.topic.ComplianceController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.topic.ComplianceReqDto;
import com.aspire.asat.cms.dto.topic.ComplianceRespDto;
import com.aspire.asat.cms.service.topic.ComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ComplianceControllerImpl implements ComplianceController {

    private final ComplianceService complianceService;

    @Override
    public ResponseEntity<ApiResponseDto<ComplianceRespDto>> createCompliance(ComplianceReqDto complianceReqDto) {
        ComplianceRespDto complianceRespDto = complianceService.createCompliance(complianceReqDto);
        ApiResponseDto<ComplianceRespDto> response = new ApiResponseDto<>("Compliance created successfully", 201, complianceRespDto);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ComplianceRespDto>> updateCompliance(String id, ComplianceReqDto complianceReqDto) {
        ComplianceRespDto complianceRespDto = complianceService.updateCompliance(id, complianceReqDto);
        ApiResponseDto<ComplianceRespDto> response = new ApiResponseDto<>("Compliance updated successfully", 200, complianceRespDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ComplianceRespDto>> getComplianceById(String id) {
        ComplianceRespDto complianceRespDto = complianceService.getById(id);
        ApiResponseDto<ComplianceRespDto> response = new ApiResponseDto<>("Compliance fetched successfully", 200, complianceRespDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteComplianceById(String id) {
        complianceService.deleteComplianceById(id);
        ApiResponseDto<Void> response = new ApiResponseDto<>("Compliance deleted successfully", 200, null);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ComplianceRespDto>>> getAllCompliances() {
        List<ComplianceRespDto> compliances = complianceService.getAllCompliance();
        ApiResponseDto<List<ComplianceRespDto>> response = new ApiResponseDto<>("Compliances fetched successfully", 200, compliances);
        return ResponseEntity.ok(response);
    }
}
