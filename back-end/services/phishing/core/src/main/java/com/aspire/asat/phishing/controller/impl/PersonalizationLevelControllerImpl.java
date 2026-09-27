package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.PersonalizationLevelController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.PersonalizationLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.PersonalizationLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.PersonalizationLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link PersonalizationLevelController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PersonalizationLevelControllerImpl implements PersonalizationLevelController {

    private final PersonalizationLevelService personalizationLevelService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<PersonalizationLevelDto>>>> getPersonalizationLevels(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<PersonalizationLevelDto> items = personalizationLevelService.getPersonalizationLevels(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = personalizationLevelService.countPersonalizationLevels(searchParam, isActive);

            AllResponseDto<List<PersonalizationLevelDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Personalization levels retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting personalization levels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve personalization levels", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<PersonalizationLevelDto>> getPersonalizationLevelById(String id) {
        try {
            PersonalizationLevelDto dto = personalizationLevelService.getPersonalizationLevelById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Personalization level retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting personalization level by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve personalization level", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<PersonalizationLevelDto>> createPersonalizationLevel(
            PersonalizationLevelCreateRequest request) {
        PersonalizationLevelDto created = personalizationLevelService.createPersonalizationLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Personalization level created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<PersonalizationLevelDto>> updatePersonalizationLevel(
            String id, PersonalizationLevelUpdateRequest request) {
        PersonalizationLevelDto updated = personalizationLevelService.updatePersonalizationLevel(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Personalization level updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deletePersonalizationLevel(String id) {
        personalizationLevelService.deletePersonalizationLevel(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Personalization level deleted successfully", 200, null));
    }
}
