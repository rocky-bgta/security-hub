package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.billing.controller.VatConfigurationController;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.VatConfigurationCreateDTO;
import com.aspire.asat.billing.dto.VatConfigurationResponseDTO;
import com.aspire.asat.billing.service.VatConfigurationService;
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
public class VatConfigurationControllerImpl implements VatConfigurationController {

    private final VatConfigurationService vatService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created VAT configuration for country: #{#dto.countryName != null ? #dto.countryName : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<VatConfigurationResponseDTO>> createVat(VatConfigurationCreateDTO dto) {
        log.info("Creating VAT configuration for country: {}", dto.getCountryName());
        VatConfigurationResponseDTO response = vatService.createVatConfiguration(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("VAT configuration created successfully", 201, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated VAT configuration for country: #{#countryId}",
            oldValueExpression = "#{#countryId}",
            newValueExpression = "#{#dto.countryName != null ? #dto.countryName : #countryId}",
            countryIdExpression = "#{#countryId}"
    )
    public ResponseEntity<ApiResponseDto<VatConfigurationResponseDTO>> updateVat(String countryId, VatConfigurationCreateDTO dto) {
        log.info("Updating VAT configuration for countryId: {}", countryId);
        VatConfigurationResponseDTO response = vatService.updateVatConfiguration(countryId, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("VAT configuration updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<VatConfigurationResponseDTO>> getVatByCountry(String countryId) {
        log.info("Getting VAT configuration for countryId: {}", countryId);
        VatConfigurationResponseDTO response = vatService.getVatByCountryId(countryId);
        return ResponseEntity.ok(new ApiResponseDto<>("VAT configuration retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<VatConfigurationResponseDTO>>>> getAll(int offset, int limit) {
        log.info("Getting all VAT configurations with offset: {}, limit: {}", offset, limit);
        List<VatConfigurationResponseDTO> response = vatService.getAllVatConfigurations(offset, limit);
        long total = vatService.countVatConfigurations();
        AllResponseDto<List<VatConfigurationResponseDTO>> allResponse = new AllResponseDto<>(offset, limit, total, response);
        return ResponseEntity.ok(new ApiResponseDto<>("VAT configurations retrieved successfully", 200, allResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> delete(String countryId) {
        log.info("Deleting VAT configuration for countryId: {}", countryId);
        vatService.deleteVatConfiguration(countryId);
        return ResponseEntity.ok(new ApiResponseDto<>("VAT configuration deleted successfully", 200, null));
    }
}
