package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.BrandCreateRequest;
import com.aspire.asat.phishing.dto.request.BrandUpdateRequest;
import com.aspire.asat.phishing.dto.response.BrandDto;

import java.util.List;

/**
 * Service for configurable brand catalog (Mongo).
 */
public interface BrandService {

    BrandDto createBrand(BrandCreateRequest request);

    BrandDto updateBrand(String id, BrandUpdateRequest request);

    void deleteBrand(String id);

    BrandDto getBrandById(String id);

    List<BrandDto> getBrands(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countBrands(String searchParam, boolean isActive);
}
