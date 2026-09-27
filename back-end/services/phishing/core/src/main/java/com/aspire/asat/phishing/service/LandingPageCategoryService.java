package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.LandingPageCategoryCreateRequest;
import com.aspire.asat.phishing.dto.request.LandingPageCategoryUpdateRequest;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;

import java.util.List;

/**
 * Service for configurable landing page category catalog (Mongo).
 */
public interface LandingPageCategoryService {

    LandingPageCategoryDto createLandingPageCategory(LandingPageCategoryCreateRequest request);

    LandingPageCategoryDto updateLandingPageCategory(String id, LandingPageCategoryUpdateRequest request);

    void deleteLandingPageCategory(String id);

    LandingPageCategoryDto getLandingPageCategoryById(String id);

    List<LandingPageCategoryDto> getLandingPageCategories(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countLandingPageCategories(String searchParam, boolean isActive);
}
