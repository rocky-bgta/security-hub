package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.ActionController;
import com.aspire.asat.common.dto.invoice_logs.ActionRequestDTO;
import com.aspire.asat.common.dto.invoice_logs.ActionResponseDTO;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.service.ActionService;
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
public class ActionControllerImpl implements ActionController {

    private final ActionService actionService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created action: #{#requestDTO.name != null ? #requestDTO.name : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<ActionResponseDTO>> createAction(ActionRequestDTO requestDTO) {
        log.info("Creating action with name: {}", requestDTO.getName());
        ActionResponseDTO response = actionService.createAction(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Action created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ActionResponseDTO>> getActionById(String id) {
        log.info("Getting action by ID: {}", id);
        ActionResponseDTO response = actionService.getActionById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Action retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ActionResponseDTO>>> getAllActions() {
        log.info("Getting all actions");
        List<ActionResponseDTO> response = actionService.getAllActions();
        return ResponseEntity.ok(new ApiResponseDto<>("Actions retrieved successfully", 200, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated action: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#requestDTO.name != null ? #requestDTO.name : #id}"
    )
    public ResponseEntity<ApiResponseDto<ActionResponseDTO>> updateAction(String id, ActionRequestDTO requestDTO) {
        log.info("Updating action with ID: {}", id);
        ActionResponseDTO response = actionService.updateAction(id, requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Action updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteAction(String id) {
        log.info("Deleting action with ID: {}", id);
        actionService.deleteAction(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Action deleted successfully", 200, null));
    }
}

