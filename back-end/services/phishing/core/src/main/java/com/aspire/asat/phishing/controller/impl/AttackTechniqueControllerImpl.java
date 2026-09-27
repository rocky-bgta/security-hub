package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.AttackTechniqueController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.AttackTechniqueCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackTechniqueUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.AttackTechniqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link AttackTechniqueController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AttackTechniqueControllerImpl implements AttackTechniqueController {

    private final AttackTechniqueService attackTechniqueService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<AttackTechniqueDto>>>> getAttackTechniques(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        try {
            List<AttackTechniqueDto> items = attackTechniqueService.getAttackTechniques(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = attackTechniqueService.countAttackTechniques(searchParam, isActive);
            AllResponseDto<List<AttackTechniqueDto>> response = new AllResponseDto<>(offset, pageSize, total, items);
            return ResponseEntity.ok(new ApiResponseDto<>("Attack techniques retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting attack techniques", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve attack techniques", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AttackTechniqueDto>> getAttackTechniqueById(String id) {
        try {
            AttackTechniqueDto dto = attackTechniqueService.getAttackTechniqueById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Attack technique retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting attack technique by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve attack technique", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AttackTechniqueDto>> createAttackTechnique(AttackTechniqueCreateRequest request) {
        AttackTechniqueDto created = attackTechniqueService.createAttackTechnique(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Attack technique created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AttackTechniqueDto>> updateAttackTechnique(
            String id, AttackTechniqueUpdateRequest request) {
        AttackTechniqueDto updated = attackTechniqueService.updateAttackTechnique(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Attack technique updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteAttackTechnique(String id) {
        attackTechniqueService.deleteAttackTechnique(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Attack technique deleted successfully", 200, null));
    }
}
