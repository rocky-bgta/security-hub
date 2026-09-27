package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.IndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.IndustryRespDto;

import java.util.List;

public interface IndustryService {
    
    IndustryRespDto createIndustry(IndustryRequestDto request);
    
    IndustryRespDto getIndustryById(String id);
    
    IndustryRespDto updateIndustry(String id, IndustryRequestDto request);

    /**
     * List active industries with an optional organization type filter.
     *
     * @param organizationTypeId optional; when set, returns only industries for that organization type
     */
    List<IndustryRespDto> getActiveIndustries(String organizationTypeId);
    
    void deleteIndustry(String id);
}
