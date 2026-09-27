package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.CampaignObjectiveCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignObjectiveUpdateRequest;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;

import java.util.List;

/**
 * Service for configurable campaign objective catalog (Mongo).
 */
public interface CampaignObjectiveService {

    CampaignObjectiveDto createCampaignObjective(CampaignObjectiveCreateRequest request);

    CampaignObjectiveDto updateCampaignObjective(String id, CampaignObjectiveUpdateRequest request);

    void deleteCampaignObjective(String id);

    CampaignObjectiveDto getCampaignObjectiveById(String id);

    List<CampaignObjectiveDto> getCampaignObjectives(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countCampaignObjectives(String searchParam, boolean isActive);
}
