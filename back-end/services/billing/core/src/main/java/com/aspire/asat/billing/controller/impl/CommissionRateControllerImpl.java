package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.CommissionRateController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.CommissionRateRequestDTO;
import com.aspire.asat.billing.dto.CommissionRateResponseDTO;
import com.aspire.asat.billing.service.CommissionRateService;
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
public class CommissionRateControllerImpl implements CommissionRateController {

    private final CommissionRateService commissionRateService;

    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_UPDATED,
            description = "Created commission rate for client: #{#request.clientId != null ? #request.clientId : 'N/A'}",
            clientAdminIdExpression = "#{#request.clientId}"
    )
    public ResponseEntity<ApiResponseDto<CommissionRateResponseDTO>> createCommissionRate(CommissionRateRequestDTO request) {
        log.info("Creating commission rate for client: {}", request.getClientId());
        CommissionRateResponseDTO result = commissionRateService.createCommissionRate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Commission rate created successfully", 201, result));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_UPDATED,
            description = "Updated commission rate for client: #{#clientId}",
            oldValueExpression = "#{#clientId}",
            newValueExpression = "#{#request.rate != null ? #request.rate.toString() : #clientId}",
            clientAdminIdExpression = "#{#clientId}"
    )
    public ResponseEntity<ApiResponseDto<CommissionRateResponseDTO>> updateCommissionRate(String clientId, CommissionRateRequestDTO request) {
        log.info("Updating commission rate for client: {}", clientId);
        CommissionRateResponseDTO result = commissionRateService.updateCommissionRate(clientId, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Commission rate updated successfully", 200, result));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CommissionRateResponseDTO>> getCommissionRateByClientId(String clientId) {
        log.info("Getting commission rate for client: {}", clientId);
        CommissionRateResponseDTO result = commissionRateService.getCommissionRateByClientId(clientId);
        return ResponseEntity.ok(new ApiResponseDto<>("Commission rate retrieved successfully", 200, result));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CommissionRateResponseDTO>>>> listCommissionRates(int offset, int limit) {
        log.info("Listing commission rates with offset: {}, limit: {}", offset, limit);
        List<CommissionRateResponseDTO> result = commissionRateService.listCommissionRates(offset, limit);
        long total = commissionRateService.countCommissionRates();
        AllResponseDto<List<CommissionRateResponseDTO>> response = new AllResponseDto<>(offset, limit, total, result);
        return ResponseEntity.ok(new ApiResponseDto<>("Commission rates retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteCommissionRate(String clientId) {
        log.info("Deleting commission rate for client: {}", clientId);
        commissionRateService.deleteCommissionRate(clientId);
        return ResponseEntity.ok(new ApiResponseDto<>("Commission rate deleted successfully", 200, null));
    }

}
