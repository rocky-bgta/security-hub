package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.SubIndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRespDto;

import java.util.List;

public interface SubIndustryService {

    SubIndustryRespDto createSubIndustry(SubIndustryRequestDto request);

    SubIndustryRespDto getSubIndustryById(String id);

    SubIndustryRespDto updateSubIndustry(String id, SubIndustryRequestDto request);

    /**
     * List active sub-industries with optional organization type and industry filters.
     *
     * @param organizationTypeId optional; when set, returns only sub-industries for that organization type
     * @param industryId         optional; when set, returns only sub-industries for that industry
     */
    List<SubIndustryRespDto> getActiveSubIndustries(String organizationTypeId, String industryId);

    /**
     * List sub-industries (active and inactive) with optional search, active, and industry filters.
     *
     * @param search     optional; matches name or code (case-insensitive)
     * @param active     optional; true/false filters by status, null returns all
     * @param industryId optional; when set, returns only sub-industries for that industry
     */
    List<SubIndustryRespDto> getSubIndustries(String search, Boolean active, String industryId);

    void deleteSubIndustry(String id);
}
