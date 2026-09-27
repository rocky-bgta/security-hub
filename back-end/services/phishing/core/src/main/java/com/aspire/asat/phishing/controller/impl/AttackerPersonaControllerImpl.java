package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.AttackerPersonaController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.AttackerPersonaCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackerPersonaUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.AttackerPersonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link AttackerPersonaController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AttackerPersonaControllerImpl implements AttackerPersonaController {

    private final AttackerPersonaService attackerPersonaService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<AttackerPersonaDto>>>> getAttackerPersonas(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<AttackerPersonaDto> items = attackerPersonaService.getAttackerPersonas(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = attackerPersonaService.countAttackerPersonas(searchParam, isActive);

            AllResponseDto<List<AttackerPersonaDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Attacker personas retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting attacker personas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve attacker personas", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AttackerPersonaDto>> getAttackerPersonaById(String id) {
        try {
            AttackerPersonaDto dto = attackerPersonaService.getAttackerPersonaById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Attacker persona retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting attacker persona by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve attacker persona", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AttackerPersonaDto>> createAttackerPersona(
            AttackerPersonaCreateRequest request) {
        AttackerPersonaDto created = attackerPersonaService.createAttackerPersona(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Attacker persona created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AttackerPersonaDto>> updateAttackerPersona(
            String id, AttackerPersonaUpdateRequest request) {
        AttackerPersonaDto updated = attackerPersonaService.updateAttackerPersona(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Attacker persona updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteAttackerPersona(String id) {
        attackerPersonaService.deleteAttackerPersona(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Attacker persona deleted successfully", 200, null));
    }
}
