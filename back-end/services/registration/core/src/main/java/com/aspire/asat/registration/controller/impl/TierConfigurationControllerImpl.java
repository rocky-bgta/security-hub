package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.tierManagement.CreateTierConfigurationRequestDTO;
import com.aspire.asat.registration.data.tierManagement.TierConfigurationResponseDTO;
import com.aspire.asat.registration.data.tierManagement.UpdateTierConfigurationRequestDTO;
import com.aspire.asat.registration.controller.TierConfigurationController;
import com.aspire.asat.registration.service.TierConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TierConfigurationControllerImpl implements TierConfigurationController {

    private final TierConfigurationService service;

    @Override
    public ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> createTier(CreateTierConfigurationRequestDTO dto) {
        TierConfigurationResponseDTO response = service.createTier(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Tier created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TierConfigurationResponseDTO>>>> getAllTiers(
            String search,
            Boolean status,
            int offset,
            int limit
    ) {
        List<TierConfigurationResponseDTO> tiers = service.getAllTiers(search, status, offset, limit);
        long total = service.getTotalTierCount(search, status);

        AllResponseDto<List<TierConfigurationResponseDTO>> allResponse =
                new AllResponseDto<>(offset, limit, total, tiers);

        return ResponseEntity.ok(
                new ApiResponseDto<>("Tier configurations fetched successfully", 200, allResponse)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> getTierByMspId(String mspId) {
        TierConfigurationResponseDTO response = service.getTierByMspId(mspId);
        return ResponseEntity.ok(new ApiResponseDto<>("Tier configuration fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> getTierById(String id) {
        TierConfigurationResponseDTO response = service.getTierById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Tier configuration fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> updateTier(String id, UpdateTierConfigurationRequestDTO dto) {
        TierConfigurationResponseDTO response = service.updateTier(id, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("Tier configuration updated successfully", 200, response));
    }

}
