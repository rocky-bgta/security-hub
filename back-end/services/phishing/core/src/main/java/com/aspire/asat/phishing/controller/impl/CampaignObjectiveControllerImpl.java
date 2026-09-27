package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.CampaignObjectiveController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.CampaignObjectiveCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignObjectiveUpdateRequest;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.CampaignObjectiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link CampaignObjectiveController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CampaignObjectiveControllerImpl implements CampaignObjectiveController {

    private final CampaignObjectiveService campaignObjectiveService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CampaignObjectiveDto>>>> getCampaignObjectives(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<CampaignObjectiveDto> items = campaignObjectiveService.getCampaignObjectives(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = campaignObjectiveService.countCampaignObjectives(searchParam, isActive);

            AllResponseDto<List<CampaignObjectiveDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Campaign objectives retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting campaign objectives", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve campaign objectives", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignObjectiveDto>> getCampaignObjectiveById(String id) {
        try {
            CampaignObjectiveDto dto = campaignObjectiveService.getCampaignObjectiveById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Campaign objective retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting campaign objective by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve campaign objective", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignObjectiveDto>> createCampaignObjective(
            CampaignObjectiveCreateRequest request) {
        CampaignObjectiveDto created = campaignObjectiveService.createCampaignObjective(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Campaign objective created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignObjectiveDto>> updateCampaignObjective(
            String id, CampaignObjectiveUpdateRequest request) {
        CampaignObjectiveDto updated = campaignObjectiveService.updateCampaignObjective(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Campaign objective updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteCampaignObjective(String id) {
        campaignObjectiveService.deleteCampaignObjective(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Campaign objective deleted successfully", 200, null));
    }
}
