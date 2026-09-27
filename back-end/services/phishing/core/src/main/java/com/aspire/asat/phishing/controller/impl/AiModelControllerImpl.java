package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.AiModelController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiModelCreateRequest;
import com.aspire.asat.phishing.dto.request.AiModelUpdateRequest;
import com.aspire.asat.phishing.dto.response.AiModelDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiModelControllerImpl implements AiModelController {

    private final AiModelService aiModelService;

    @Override
    public ResponseEntity<ApiResponseDto<AiModelDto>> createAiModel(AiModelCreateRequest request) {
        try {
            AiModelDto created = aiModelService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>("AI model created", 201, created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error creating AI model", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to create AI model", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AiModelDto>> getAiModelById(String id) {
        try {
            AiModelDto dto = aiModelService.getById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("AI model retrieved", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting AI model", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve AI model", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AiModelDto>> updateAiModel(String id, AiModelUpdateRequest request) {
        try {
            AiModelDto updated = aiModelService.update(id, request);
            return ResponseEntity.ok(new ApiResponseDto<>("AI model updated", 200, updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error updating AI model", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update AI model", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<AiModelDto>>> listAiModels(AiProviderType providerType) {
        try {
            List<AiModelDto> list = aiModelService.list(providerType);
            return ResponseEntity.ok(new ApiResponseDto<>("AI models retrieved", 200, list));
        } catch (Exception e) {
            log.error("Error listing AI models", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to list AI models", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteAiModel(String id) {
        try {
            aiModelService.delete(id);
            return ResponseEntity.ok(new ApiResponseDto<>("AI model deleted", 200, null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error deleting AI model", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to delete AI model", 500, null));
        }
    }
}
