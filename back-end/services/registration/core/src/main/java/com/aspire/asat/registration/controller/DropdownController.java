package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.dropdown.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DropdownController {
    
    // Country APIs
    ResponseEntity<ApiResponse<CountryRespDto>> createCountry(CountryRequestDto request);
    ResponseEntity<ApiResponse<List<CountryRespDto>>> getAllCountries();
    ResponseEntity<ApiResponse<List<CountryRespDto>>> getActiveCountries();
    ResponseEntity<ApiResponse<CountryRespDto>> getCountryById(String id);
    ResponseEntity<ApiResponse<CountryRespDto>> getCountryByCode(String code);
    ResponseEntity<ApiResponse<CountryRespDto>> getCountryByPhoneCode(String phoneCode);
    ResponseEntity<ApiResponse<CountryRespDto>> updateCountry(String id, CountryRequestDto request);
    ResponseEntity<ApiResponse<Void>> deleteCountry(String id);
    
    // State APIs
    ResponseEntity<ApiResponse<StateRespDto>> createState(StateRequestDto request);
    ResponseEntity<ApiResponse<List<StateRespDto>>> getAllStates();
    ResponseEntity<ApiResponse<List<StateRespDto>>> getActiveStates();
    ResponseEntity<ApiResponse<List<StateRespDto>>> getStatesByCountryId(String countryId);
    ResponseEntity<ApiResponse<List<StateRespDto>>> getActiveStatesByCountryId(String countryId);
    ResponseEntity<ApiResponse<StateRespDto>> getStateById(String id);
    ResponseEntity<ApiResponse<StateRespDto>> getStateByCode(String code);
    ResponseEntity<ApiResponse<StateRespDto>> updateState(String id, StateRequestDto request);
    ResponseEntity<ApiResponse<Void>> deleteState(String id);
    
    // Timezone APIs
    ResponseEntity<ApiResponse<TimezoneRespDto>> createTimezone(TimezoneRequestDto request);
    ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getAllTimezones();
    ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getActiveTimezones();
    ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getTimezonesByStateId(String stateId);
    ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getActiveTimezonesByStateId(String stateId);
    ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getTimezonesByCountryId(String countryId);
    ResponseEntity<ApiResponse<TimezoneRespDto>> getTimezoneById(String id);
    ResponseEntity<ApiResponse<TimezoneRespDto>> getTimezoneByTimezoneId(String timezoneId);
    ResponseEntity<ApiResponse<TimezoneRespDto>> updateTimezone(String id, TimezoneRequestDto request);
    ResponseEntity<ApiResponse<Void>> deleteTimezone(String id);
    
    // Language APIs
    ResponseEntity<ApiResponse<LanguageRespDto>> createLanguage(LanguageRequestDto request);
    ResponseEntity<ApiResponse<LanguageRespDto>> getLanguageById(String id);
    ResponseEntity<ApiResponse<List<LanguageRespDto>>> getActiveLanguages();
    ResponseEntity<ApiResponse<LanguageRespDto>> updateLanguage(String id, LanguageRequestDto request);
    ResponseEntity<ApiResponse<Void>> deleteLanguage(String id);
    
    // Industry APIs
    ResponseEntity<ApiResponse<IndustryRespDto>> createIndustry(IndustryRequestDto request);
    ResponseEntity<ApiResponse<IndustryRespDto>> getIndustryById(String id);
    ResponseEntity<ApiResponse<IndustryRespDto>> updateIndustry(String id, IndustryRequestDto request);
    ResponseEntity<ApiResponse<List<IndustryRespDto>>> getActiveIndustries(String organizationTypeId);
    ResponseEntity<ApiResponse<Void>> deleteIndustry(String id);

    // SubIndustry APIs
    ResponseEntity<ApiResponse<SubIndustryRespDto>> createSubIndustry(SubIndustryRequestDto request);
    ResponseEntity<ApiResponse<SubIndustryRespDto>> getSubIndustryById(String id);
    ResponseEntity<ApiResponse<SubIndustryRespDto>> updateSubIndustry(String id, SubIndustryRequestDto request);
    ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> getActiveSubIndustries(String organizationTypeId, String industryId);
    ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> getSubIndustries(String search, Boolean active, String industryId);
    ResponseEntity<ApiResponse<Void>> deleteSubIndustry(String id);

    // Organization Size APIs
    ResponseEntity<ApiResponse<OrganizationSizeRespDto>> createOrganizationSize(OrganizationSizeRequestDto request);
    ResponseEntity<ApiResponse<OrganizationSizeRespDto>> getOrganizationSizeById(String id);
    ResponseEntity<ApiResponse<OrganizationSizeRespDto>> updateOrganizationSize(String id, OrganizationSizeRequestDto request);
    ResponseEntity<ApiResponse<List<OrganizationSizeRespDto>>> getActiveOrganizationSizes();
    ResponseEntity<ApiResponse<Void>> deleteOrganizationSize(String id);

    // MspType APIs
    ResponseEntity<ApiResponse<MspTypeRespDto>> createMspType(MspTypeRequestDto request);
    ResponseEntity<ApiResponse<MspTypeRespDto>> getMspTypeById(String id);
    ResponseEntity<ApiResponse<MspTypeRespDto>> updateMspType(String id, MspTypeRequestDto request);
    ResponseEntity<ApiResponse<List<MspTypeRespDto>>> getActiveMspTypes();
    ResponseEntity<ApiResponse<Void>> deleteMspType(String id);

    // SuspendReason APIs
    ResponseEntity<ApiResponse<SuspendReasonRespDto>> createSuspendReason(SuspendReasonRequestDto request);
    ResponseEntity<ApiResponse<List<SuspendReasonRespDto>>> getAllSuspendReasons();
    ResponseEntity<ApiResponse<List<SuspendReasonRespDto>>> getActiveSuspendReasons();
    ResponseEntity<ApiResponse<SuspendReasonRespDto>> getSuspendReasonById(String id);
    ResponseEntity<ApiResponse<SuspendReasonRespDto>> updateSuspendReason(String id, SuspendReasonRequestDto request);
    ResponseEntity<ApiResponse<Void>> deleteSuspendReason(String id);
    ResponseEntity<ApiResponse<SuspendReasonRespDto>> updateSuspendReasonStatus(String id, Boolean active);
}
