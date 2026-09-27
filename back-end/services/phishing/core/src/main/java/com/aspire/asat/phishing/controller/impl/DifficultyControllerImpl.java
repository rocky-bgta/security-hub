package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.DifficultyController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.DifficultyCreateRequest;
import com.aspire.asat.phishing.dto.request.DifficultyUpdateRequest;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.DifficultyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link DifficultyController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DifficultyControllerImpl implements DifficultyController {

    private final DifficultyService difficultyService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<DifficultyDto>>>> getDifficulties(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<DifficultyDto> items = difficultyService.getDifficulties(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = difficultyService.countDifficulties(searchParam, isActive);

            AllResponseDto<List<DifficultyDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Difficulties retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting difficulties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve difficulties", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DifficultyDto>> getDifficultyById(String id) {
        try {
            DifficultyDto dto = difficultyService.getDifficultyById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Difficulty retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting difficulty by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve difficulty", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DifficultyDto>> createDifficulty(DifficultyCreateRequest request) {
        DifficultyDto created = difficultyService.createDifficulty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Difficulty created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<DifficultyDto>> updateDifficulty(
            String id, DifficultyUpdateRequest request) {
        DifficultyDto updated = difficultyService.updateDifficulty(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Difficulty updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteDifficulty(String id) {
        difficultyService.deleteDifficulty(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Difficulty deleted successfully", 200, null));
    }
}
