package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.BrandController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.BrandCreateRequest;
import com.aspire.asat.phishing.dto.request.BrandUpdateRequest;
import com.aspire.asat.phishing.dto.response.BrandDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link BrandController}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class BrandControllerImpl implements BrandController {

    private final BrandService brandService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<BrandDto>>>> getBrands(
            String searchParam,
            boolean isActive,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        try {
            List<BrandDto> items = brandService.getBrands(
                    searchParam, isActive, offset, pageSize, sortBy, sortOrder);
            long total = brandService.countBrands(searchParam, isActive);

            AllResponseDto<List<BrandDto>> response =
                    new AllResponseDto<>(offset, pageSize, total, items);

            return ResponseEntity.ok(
                    new ApiResponseDto<>("Brands retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting brands", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve brands", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BrandDto>> getBrandById(String id) {
        try {
            BrandDto dto = brandService.getBrandById(id);
            return ResponseEntity.ok(new ApiResponseDto<>("Brand retrieved successfully", 200, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error getting brand by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve brand", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BrandDto>> createBrand(BrandCreateRequest request) {
        BrandDto created = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Brand created successfully", 201, created));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BrandDto>> updateBrand(String id, BrandUpdateRequest request) {
        BrandDto updated = brandService.updateBrand(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Brand updated successfully", 200, updated));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteBrand(String id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Brand deleted successfully", 200, null));
    }
}
