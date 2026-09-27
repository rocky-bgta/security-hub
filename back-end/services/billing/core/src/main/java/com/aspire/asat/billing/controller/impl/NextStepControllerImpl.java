package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.NextStepController;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.common.dto.invoice_logs.NextStepRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.NextStepResponseDTO;
import com.aspire.asat.billing.service.NextStepService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class NextStepControllerImpl implements NextStepController {

    private final NextStepService nextStepService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created next step: #{#requestDTO.name != null ? #requestDTO.name : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<NextStepResponseDTO>> createNextStep(NextStepRequestDTO requestDTO) {
        log.info("Creating next step with name: {}", requestDTO.getName());
        NextStepResponseDTO response = nextStepService.createNextStep(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Next step created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<NextStepResponseDTO>> getNextStepById(String id) {
        log.info("Getting next step by ID: {}", id);
        NextStepResponseDTO response = nextStepService.getNextStepById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Next step retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<NextStepResponseDTO>>> getAllNextSteps() {
        log.info("Getting all next steps");
        List<NextStepResponseDTO> response = nextStepService.getAllNextSteps();
        return ResponseEntity.ok(new ApiResponseDto<>("Next steps retrieved successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated next step: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDTO.name != null ? #requestDTO.name : #id}"
    )
    public ResponseEntity<ApiResponseDto<NextStepResponseDTO>> updateNextStep(String id, NextStepRequestDTO requestDTO) {
        log.info("Updating next step with ID: {}", id);
        NextStepResponseDTO response = nextStepService.updateNextStep(id, requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Next step updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteNextStep(String id) {
        log.info("Deleting next step with ID: {}", id);
        nextStepService.deleteNextStep(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Next step deleted successfully", 200, null));
    }
}

