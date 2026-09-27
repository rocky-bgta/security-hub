package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyCreateRequest;
import com.aspire.asat.phishing.dto.request.SocialEngineeringStrategyUpdateRequest;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;

import java.util.List;

/**
 * Service for configurable social engineering strategy catalog (Mongo).
 */
public interface SocialEngineeringStrategyService {

    SocialEngineeringStrategyDto createSocialEngineeringStrategy(SocialEngineeringStrategyCreateRequest request);

    SocialEngineeringStrategyDto updateSocialEngineeringStrategy(String id, SocialEngineeringStrategyUpdateRequest request);

    void deleteSocialEngineeringStrategy(String id);

    SocialEngineeringStrategyDto getSocialEngineeringStrategyById(String id);

    List<SocialEngineeringStrategyDto> getSocialEngineeringStrategies(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countSocialEngineeringStrategies(String searchParam, boolean isActive);
}
