package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.SocialEngineeringStrategyController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyCreateRequest;
import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyUpdateRequest;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.SocialEngineeringStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link SocialEngineeringStrategyController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class SocialEngineeringStrategyControllerImpl implements SocialEngineeringStrategyController {

    private final SocialEngineeringStrategyService socialEngineeringStrategyService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SocialEngineeringStrategyDto>>>> getSocialEngineeringStrategies(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<SocialEngineeringStrategyDto> items = socialEngineeringStrategyService.getSocialEngineeringStrategies(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = socialEngineeringStrategyService.countSocialEngineeringStrategies(searchParam, isActive);

            AllResponseDto<List<SocialEngineeringStrategyDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Social engineering strategies retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting social engineering strategies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve social engineering strategies", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SocialEngineeringStrategyDto>> getSocialEngineeringStrategyById(String id) {
        try {
            SocialEngineeringStrategyDto dto = socialEngineeringStrategyService.getSocialEngineeringStrategyById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Social engineering strategy retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting social engineering strategy by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve social engineering strategy", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SocialEngineeringStrategyDto>> createSocialEngineeringStrategy(
            SocialEngineeringStrategyCreateRequest request) {
        SocialEngineeringStrategyDto created = socialEngineeringStrategyService.createSocialEngineeringStrategy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Social engineering strategy created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SocialEngineeringStrategyDto>> updateSocialEngineeringStrategy(
            String id, SocialEngineeringStrategyUpdateRequest request) {
        SocialEngineeringStrategyDto updated =
                socialEngineeringStrategyService.updateSocialEngineeringStrategy(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Social engineering strategy updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteSocialEngineeringStrategy(String id) {
        socialEngineeringStrategyService.deleteSocialEngineeringStrategy(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Social engineering strategy deleted successfully", 200, null));
    }
}
