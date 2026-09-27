package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.dropdown.*;
import com.aspire.asat.registration.controller.DropdownController;
import com.aspire.asat.registration.service.dropdown.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dropdown")
@RequiredArgsConstructor
@Slf4j
public class DropdownControllerImpl implements DropdownController {
    
    private final CountryService countryService;
    private final StateService stateService;
    private final TimezoneService timezoneService;
    private final LanguageService languageService;
    private final IndustryService industryService;
    private final SubIndustryService subIndustryService;
    private final OrganizationSizeService organizationSizeService;
    private final MspTypeService mspTypeService;
    private final SuspendReasonService suspendReasonService;
    
    // Country APIs
    @Override
    @PostMapping("/countries")
    public ResponseEntity<ApiResponse<CountryRespDto>> createCountry(@RequestBody CountryRequestDto request) {
        log.info("Creating country: {}", request.getName());
        CountryRespDto country = countryService.createCountry(request);
        ApiResponse<CountryRespDto> response = ApiResponse.<CountryRespDto>builder()
                .message("Country created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(country)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<CountryRespDto>>> getAllCountries() {
        log.info("Fetching all countries");
        List<CountryRespDto> countries = countryService.getAllCountries();
        ApiResponse<List<CountryRespDto>> response = ApiResponse.<List<CountryRespDto>>builder()
                .message("Countries retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(countries)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/countries/active")
    public ResponseEntity<ApiResponse<List<CountryRespDto>>> getActiveCountries() {
        log.info("Fetching active countries");
        List<CountryRespDto> countries = countryService.getActiveCountries();
        ApiResponse<List<CountryRespDto>> response = ApiResponse.<List<CountryRespDto>>builder()
                .message("Active countries retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(countries)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/countries/{id}")
    public ResponseEntity<ApiResponse<CountryRespDto>> getCountryById(@PathVariable String id) {
        log.info("Fetching country by id: {}", id);
        CountryRespDto country = countryService.getCountryById(id);
        ApiResponse<CountryRespDto> response = ApiResponse.<CountryRespDto>builder()
                .message("Country retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(country)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/countries/code/{code}")
    public ResponseEntity<ApiResponse<CountryRespDto>> getCountryByCode(@PathVariable String code) {
        log.info("Fetching country by code: {}", code);
        CountryRespDto country = countryService.getCountryByCode(code);
        ApiResponse<CountryRespDto> response = ApiResponse.<CountryRespDto>builder()
                .message("Country retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(country)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/countries/phone-code/{phoneCode}")
    public ResponseEntity<ApiResponse<CountryRespDto>> getCountryByPhoneCode(@PathVariable String phoneCode) {
        log.info("Fetching country by phone code: {}", phoneCode);
        CountryRespDto country = countryService.getCountryByPhoneCode(phoneCode);
        ApiResponse<CountryRespDto> response = ApiResponse.<CountryRespDto>builder()
                .message("Country retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(country)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @PutMapping("/countries/{id}")
    public ResponseEntity<ApiResponse<CountryRespDto>> updateCountry(@PathVariable String id, @RequestBody CountryRequestDto request) {
        log.info("Updating country with id: {}", id);
        CountryRespDto country = countryService.updateCountry(id, request);
        ApiResponse<CountryRespDto> response = ApiResponse.<CountryRespDto>builder()
                .message("Country updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(country)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @DeleteMapping("/countries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable String id) {
        log.info("Deleting country with id: {}", id);
        countryService.deleteCountry(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Country deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
    
    // State APIs
    @Override
    @PostMapping("/states")
    public ResponseEntity<ApiResponse<StateRespDto>> createState(@RequestBody StateRequestDto request) {
        log.info("Creating state: {} for country: {}", request.getName(), request.getCountryId());
        StateRespDto state = stateService.createState(request);
        ApiResponse<StateRespDto> response = ApiResponse.<StateRespDto>builder()
                .message("State created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(state)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Override
    @GetMapping("/states")
    public ResponseEntity<ApiResponse<List<StateRespDto>>> getAllStates() {
        log.info("Fetching all states");
        List<StateRespDto> states = stateService.getAllStates();
        ApiResponse<List<StateRespDto>> response = ApiResponse.<List<StateRespDto>>builder()
                .message("States retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(states)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/states/active")
    public ResponseEntity<ApiResponse<List<StateRespDto>>> getActiveStates() {
        log.info("Fetching active states");
        List<StateRespDto> states = stateService.getActiveStates();
        ApiResponse<List<StateRespDto>> response = ApiResponse.<List<StateRespDto>>builder()
                .message("Active states retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(states)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/states/country/{countryId}")
    public ResponseEntity<ApiResponse<List<StateRespDto>>> getStatesByCountryId(@PathVariable String countryId) {
        log.info("Fetching states for country: {}", countryId);
        List<StateRespDto> states = stateService.getStatesByCountryId(countryId);
        ApiResponse<List<StateRespDto>> response = ApiResponse.<List<StateRespDto>>builder()
                .message("States retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(states)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/states/country/{countryId}/active")
    public ResponseEntity<ApiResponse<List<StateRespDto>>> getActiveStatesByCountryId(@PathVariable String countryId) {
        log.info("Fetching active states for country: {}", countryId);
        List<StateRespDto> states = stateService.getActiveStatesByCountryId(countryId);
        ApiResponse<List<StateRespDto>> response = ApiResponse.<List<StateRespDto>>builder()
                .message("Active states retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(states)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/states/{id}")
    public ResponseEntity<ApiResponse<StateRespDto>> getStateById(@PathVariable String id) {
        log.info("Fetching state by id: {}", id);
        StateRespDto state = stateService.getStateById(id);
        ApiResponse<StateRespDto> response = ApiResponse.<StateRespDto>builder()
                .message("State retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(state)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/states/code/{code}")
    public ResponseEntity<ApiResponse<StateRespDto>> getStateByCode(@PathVariable String code) {
        log.info("Fetching state by code: {}", code);
        StateRespDto state = stateService.getStateByCode(code);
        ApiResponse<StateRespDto> response = ApiResponse.<StateRespDto>builder()
                .message("State retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(state)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @PutMapping("/states/{id}")
    public ResponseEntity<ApiResponse<StateRespDto>> updateState(@PathVariable String id, @RequestBody StateRequestDto request) {
        log.info("Updating state with id: {}", id);
        StateRespDto state = stateService.updateState(id, request);
        ApiResponse<StateRespDto> response = ApiResponse.<StateRespDto>builder()
                .message("State updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(state)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @DeleteMapping("/states/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable String id) {
        log.info("Deleting state with id: {}", id);
        stateService.deleteState(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("State deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
    
    // Timezone APIs
    @Override
    @PostMapping("/timezones")
    public ResponseEntity<ApiResponse<TimezoneRespDto>> createTimezone(@RequestBody TimezoneRequestDto request) {
        log.info("Creating timezone: {} for state: {}", request.getDisplayName(), request.getStateId());
        TimezoneRespDto timezone = timezoneService.createTimezone(request);
        ApiResponse<TimezoneRespDto> response = ApiResponse.<TimezoneRespDto>builder()
                .message("Timezone created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(timezone)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Override
    @GetMapping("/timezones")
    public ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getAllTimezones() {
        log.info("Fetching all timezones");
        List<TimezoneRespDto> timezones = timezoneService.getAllTimezones();
        ApiResponse<List<TimezoneRespDto>> response = ApiResponse.<List<TimezoneRespDto>>builder()
                .message("Timezones retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezones)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/timezones/active")
    public ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getActiveTimezones() {
        log.info("Fetching active timezones");
        List<TimezoneRespDto> timezones = timezoneService.getActiveTimezones();
        ApiResponse<List<TimezoneRespDto>> response = ApiResponse.<List<TimezoneRespDto>>builder()
                .message("Active timezones retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezones)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/timezones/state/{stateId}")
    public ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getTimezonesByStateId(@PathVariable String stateId) {
        log.info("Fetching timezones for state: {}", stateId);
        List<TimezoneRespDto> timezones = timezoneService.getTimezonesByStateId(stateId);
        ApiResponse<List<TimezoneRespDto>> response = ApiResponse.<List<TimezoneRespDto>>builder()
                .message("Timezones retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezones)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/timezones/state/{stateId}/active")
    public ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getActiveTimezonesByStateId(@PathVariable String stateId) {
        log.info("Fetching active timezones for state: {}", stateId);
        List<TimezoneRespDto> timezones = timezoneService.getActiveTimezonesByStateId(stateId);
        ApiResponse<List<TimezoneRespDto>> response = ApiResponse.<List<TimezoneRespDto>>builder()
                .message("Active timezones retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezones)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/timezones/country/{countryId}")
    public ResponseEntity<ApiResponse<List<TimezoneRespDto>>> getTimezonesByCountryId(@PathVariable String countryId) {
        log.info("Fetching timezones for country: {}", countryId);
        List<TimezoneRespDto> timezones = timezoneService.getTimezonesByCountryId(countryId);
        ApiResponse<List<TimezoneRespDto>> response = ApiResponse.<List<TimezoneRespDto>>builder()
                .message("Timezones retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezones)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/timezones/{id}")
    public ResponseEntity<ApiResponse<TimezoneRespDto>> getTimezoneById(@PathVariable String id) {
        log.info("Fetching timezone by id: {}", id);
        TimezoneRespDto timezone = timezoneService.getTimezoneById(id);
        ApiResponse<TimezoneRespDto> response = ApiResponse.<TimezoneRespDto>builder()
                .message("Timezone retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezone)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/timezones/timezone-id/{timezoneId}")
    public ResponseEntity<ApiResponse<TimezoneRespDto>> getTimezoneByTimezoneId(@PathVariable String timezoneId) {
        log.info("Fetching timezone by timezoneId: {}", timezoneId);
        TimezoneRespDto timezone = timezoneService.getTimezoneByTimezoneId(timezoneId);
        ApiResponse<TimezoneRespDto> response = ApiResponse.<TimezoneRespDto>builder()
                .message("Timezone retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezone)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @PutMapping("/timezones/{id}")
    public ResponseEntity<ApiResponse<TimezoneRespDto>> updateTimezone(@PathVariable String id, @RequestBody TimezoneRequestDto request) {
        log.info("Updating timezone with id: {}", id);
        TimezoneRespDto timezone = timezoneService.updateTimezone(id, request);
        ApiResponse<TimezoneRespDto> response = ApiResponse.<TimezoneRespDto>builder()
                .message("Timezone updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(timezone)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @DeleteMapping("/timezones/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimezone(@PathVariable String id) {
        log.info("Deleting timezone with id: {}", id);
        timezoneService.deleteTimezone(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Timezone deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
    
    // Language APIs
    @Override
    @PostMapping("/languages")
    public ResponseEntity<ApiResponse<LanguageRespDto>> createLanguage(@RequestBody LanguageRequestDto request) {
        log.info("Creating language: {}", request.getDisplayName());
        LanguageRespDto language = languageService.createLanguage(request);
        ApiResponse<LanguageRespDto> response = ApiResponse.<LanguageRespDto>builder()
                .message("Language created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(language)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Override
    @GetMapping("/languages/{id}")
    public ResponseEntity<ApiResponse<LanguageRespDto>> getLanguageById(@PathVariable String id) {
        log.info("Fetching language by id: {}", id);
        LanguageRespDto language = languageService.getLanguageById(id);
        ApiResponse<LanguageRespDto> response = ApiResponse.<LanguageRespDto>builder()
                .message("Language retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(language)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/languages/active")
    public ResponseEntity<ApiResponse<List<LanguageRespDto>>> getActiveLanguages() {
        log.info("Fetching active languages");
        List<LanguageRespDto> languages = languageService.getActiveLanguages();
        ApiResponse<List<LanguageRespDto>> response = ApiResponse.<List<LanguageRespDto>>builder()
                .message("Active languages retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(languages)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @PutMapping("/languages/{id}")
    public ResponseEntity<ApiResponse<LanguageRespDto>> updateLanguage(@PathVariable String id, @RequestBody LanguageRequestDto request) {
        log.info("Updating language with id: {}", id);
        LanguageRespDto language = languageService.updateLanguage(id, request);
        ApiResponse<LanguageRespDto> response = ApiResponse.<LanguageRespDto>builder()
                .message("Language updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(language)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @DeleteMapping("/languages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLanguage(@PathVariable String id) {
        log.info("Deleting language with id: {}", id);
        languageService.deleteLanguage(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Language deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
    
    // Industry APIs
    @Override
    @PostMapping("/industries")
    public ResponseEntity<ApiResponse<IndustryRespDto>> createIndustry(@RequestBody IndustryRequestDto request) {
        log.info("Creating industry: {}", request.getName());
        IndustryRespDto industry = industryService.createIndustry(request);
        ApiResponse<IndustryRespDto> response = ApiResponse.<IndustryRespDto>builder()
                .message("Industry created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(industry)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Override
    @GetMapping("/industries/{id}")
    public ResponseEntity<ApiResponse<IndustryRespDto>> getIndustryById(@PathVariable String id) {
        log.info("Fetching industry by id: {}", id);
        IndustryRespDto industry = industryService.getIndustryById(id);
        ApiResponse<IndustryRespDto> response = ApiResponse.<IndustryRespDto>builder()
                .message("Industry retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(industry)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @PutMapping("/industries/{id}")
    public ResponseEntity<ApiResponse<IndustryRespDto>> updateIndustry(@PathVariable String id, @RequestBody IndustryRequestDto request) {
        log.info("Updating industry with id: {}", id);
        IndustryRespDto industry = industryService.updateIndustry(id, request);
        ApiResponse<IndustryRespDto> response = ApiResponse.<IndustryRespDto>builder()
                .message("Industry updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(industry)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/industries/active")
    public ResponseEntity<ApiResponse<List<IndustryRespDto>>> getActiveIndustries(
            @RequestParam(required = false) String organizationTypeId) {
        log.info("Fetching active industries with organizationTypeId: {}", organizationTypeId);
        List<IndustryRespDto> industries = industryService.getActiveIndustries(organizationTypeId);
        ApiResponse<List<IndustryRespDto>> response = ApiResponse.<List<IndustryRespDto>>builder()
                .message("Active industries retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(industries)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @DeleteMapping("/industries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIndustry(@PathVariable String id) {
        log.info("Deleting industry with id: {}", id);
        industryService.deleteIndustry(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Industry deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    // SubIndustry APIs
    @Override
    @PostMapping("/sub-industries")
    public ResponseEntity<ApiResponse<SubIndustryRespDto>> createSubIndustry(@RequestBody SubIndustryRequestDto request) {
        log.info("Creating sub-industry: {}", request.getName());
        SubIndustryRespDto subIndustry = subIndustryService.createSubIndustry(request);
        ApiResponse<SubIndustryRespDto> response = ApiResponse.<SubIndustryRespDto>builder()
                .message("Sub-industry created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(subIndustry)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/sub-industries")
    public ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> getSubIndustries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String industryId) {
        log.info("Fetching sub-industries with search: {}, active: {}, industryId: {}", search, active, industryId);
        List<SubIndustryRespDto> subIndustries = subIndustryService.getSubIndustries(search, active, industryId);
        ApiResponse<List<SubIndustryRespDto>> response = ApiResponse.<List<SubIndustryRespDto>>builder()
                .message("Sub-industries retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(subIndustries)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/sub-industries/{id}")
    public ResponseEntity<ApiResponse<SubIndustryRespDto>> getSubIndustryById(@PathVariable String id) {
        log.info("Fetching sub-industry by id: {}", id);
        SubIndustryRespDto subIndustry = subIndustryService.getSubIndustryById(id);
        ApiResponse<SubIndustryRespDto> response = ApiResponse.<SubIndustryRespDto>builder()
                .message("Sub-industry retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(subIndustry)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @PutMapping("/sub-industries/{id}")
    public ResponseEntity<ApiResponse<SubIndustryRespDto>> updateSubIndustry(@PathVariable String id, @RequestBody SubIndustryRequestDto request) {
        log.info("Updating sub-industry with id: {}", id);
        SubIndustryRespDto subIndustry = subIndustryService.updateSubIndustry(id, request);
        ApiResponse<SubIndustryRespDto> response = ApiResponse.<SubIndustryRespDto>builder()
                .message("Sub-industry updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(subIndustry)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/sub-industries/active")
    public ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> getActiveSubIndustries(
            @RequestParam(required = false) String organizationTypeId,
            @RequestParam(required = false) String industryId) {
        log.info("Fetching active sub-industries with organizationTypeId: {}, industryId: {}",
                organizationTypeId, industryId);
        List<SubIndustryRespDto> subIndustries =
                subIndustryService.getActiveSubIndustries(organizationTypeId, industryId);
        ApiResponse<List<SubIndustryRespDto>> response = ApiResponse.<List<SubIndustryRespDto>>builder()
                .message("Active sub-industries retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(subIndustries)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/sub-industries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubIndustry(@PathVariable String id) {
        log.info("Deleting sub-industry with id: {}", id);
        subIndustryService.deleteSubIndustry(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Sub-industry deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    // Organization Size APIs
    @Override
    @PostMapping("/organization-sizes")
    public ResponseEntity<ApiResponse<OrganizationSizeRespDto>> createOrganizationSize(@RequestBody OrganizationSizeRequestDto request) {
        log.info("Creating organization size: {} with range: {}", request.getName(), request.getRange());
        OrganizationSizeRespDto organizationSize = organizationSizeService.createOrganizationSize(request);
        ApiResponse<OrganizationSizeRespDto> response = ApiResponse.<OrganizationSizeRespDto>builder()
                .message("Organization size created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(organizationSize)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Override
    @GetMapping("/organization-sizes/{id}")
    public ResponseEntity<ApiResponse<OrganizationSizeRespDto>> getOrganizationSizeById(@PathVariable String id) {
        log.info("Fetching organization size by id: {}", id);
        OrganizationSizeRespDto organizationSize = organizationSizeService.getOrganizationSizeById(id);
        ApiResponse<OrganizationSizeRespDto> response = ApiResponse.<OrganizationSizeRespDto>builder()
                .message("Organization size retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(organizationSize)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @PutMapping("/organization-sizes/{id}")
    public ResponseEntity<ApiResponse<OrganizationSizeRespDto>> updateOrganizationSize(@PathVariable String id, @RequestBody OrganizationSizeRequestDto request) {
        log.info("Updating organization size with id: {}", id);
        OrganizationSizeRespDto organizationSize = organizationSizeService.updateOrganizationSize(id, request);
        ApiResponse<OrganizationSizeRespDto> response = ApiResponse.<OrganizationSizeRespDto>builder()
                .message("Organization size updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(organizationSize)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @GetMapping("/organization-sizes/active")
    public ResponseEntity<ApiResponse<List<OrganizationSizeRespDto>>> getActiveOrganizationSizes() {
        log.info("Fetching active organization sizes");
        List<OrganizationSizeRespDto> organizationSizes = organizationSizeService.getActiveOrganizationSizes();
        ApiResponse<List<OrganizationSizeRespDto>> response = ApiResponse.<List<OrganizationSizeRespDto>>builder()
                .message("Organization sizes retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(organizationSizes)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    @DeleteMapping("/organization-sizes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganizationSize(@PathVariable String id) {
        log.info("Deleting organization size with id: {}", id);
        organizationSizeService.deleteOrganizationSize(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Organization size deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    // MspType APIs
    @Override
    @PostMapping("/msp-types")
    public ResponseEntity<ApiResponse<MspTypeRespDto>> createMspType(@RequestBody MspTypeRequestDto request) {
        log.info("Creating MspType: {}", request.getName());
        MspTypeRespDto mspType = mspTypeService.createMspType(request);
        ApiResponse<MspTypeRespDto> response = ApiResponse.<MspTypeRespDto>builder()
                .message("MspType created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(mspType)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/msp-types/{id}")
    public ResponseEntity<ApiResponse<MspTypeRespDto>> getMspTypeById(@PathVariable String id) {
        log.info("Fetching MspType by id: {}", id);
        MspTypeRespDto mspType = mspTypeService.getMspTypeById(id);
        ApiResponse<MspTypeRespDto> response = ApiResponse.<MspTypeRespDto>builder()
                .message("MspType retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(mspType)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @PutMapping("/msp-types/{id}")
    public ResponseEntity<ApiResponse<MspTypeRespDto>> updateMspType(@PathVariable String id, @RequestBody MspTypeRequestDto request) {
        log.info("Updating MspType with id: {}", id);
        MspTypeRespDto mspType = mspTypeService.updateMspType(id, request);
        ApiResponse<MspTypeRespDto> response = ApiResponse.<MspTypeRespDto>builder()
                .message("MspType updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(mspType)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/msp-types/active")
    public ResponseEntity<ApiResponse<List<MspTypeRespDto>>> getActiveMspTypes() {
        log.info("Fetching active MspTypes");
        List<MspTypeRespDto> mspTypes = mspTypeService.getActiveMspTypes();
        ApiResponse<List<MspTypeRespDto>> response = ApiResponse.<List<MspTypeRespDto>>builder()
                .message("Active MspTypes retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(mspTypes)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/msp-types/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMspType(@PathVariable String id) {
        log.info("Deleting MspType with id: {}", id);
        mspTypeService.deleteMspType(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("MspType deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    // SuspendReason APIs
    @Override
    @PostMapping("/suspend-reasons")
    public ResponseEntity<ApiResponse<SuspendReasonRespDto>> createSuspendReason(@RequestBody SuspendReasonRequestDto request) {
        log.info("Creating suspend reason: {}", request.getName());
        SuspendReasonRespDto suspendReason = suspendReasonService.createSuspendReason(request);
        ApiResponse<SuspendReasonRespDto> response = ApiResponse.<SuspendReasonRespDto>builder()
                .message("Suspend reason created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .data(suspendReason)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/suspend-reasons")
    public ResponseEntity<ApiResponse<List<SuspendReasonRespDto>>> getAllSuspendReasons() {
        log.info("Fetching all suspend reasons");
        List<SuspendReasonRespDto> suspendReasons = suspendReasonService.getAllSuspendReasons();
        ApiResponse<List<SuspendReasonRespDto>> response = ApiResponse.<List<SuspendReasonRespDto>>builder()
                .message("Suspend reasons retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(suspendReasons)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/suspend-reasons/active")
    public ResponseEntity<ApiResponse<List<SuspendReasonRespDto>>> getActiveSuspendReasons() {
        log.info("Fetching active suspend reasons");
        List<SuspendReasonRespDto> suspendReasons = suspendReasonService.getActiveSuspendReasons();
        ApiResponse<List<SuspendReasonRespDto>> response = ApiResponse.<List<SuspendReasonRespDto>>builder()
                .message("Active suspend reasons retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(suspendReasons)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/suspend-reasons/{id}")
    public ResponseEntity<ApiResponse<SuspendReasonRespDto>> getSuspendReasonById(@PathVariable String id) {
        log.info("Fetching suspend reason by id: {}", id);
        SuspendReasonRespDto suspendReason = suspendReasonService.getSuspendReasonById(id);
        ApiResponse<SuspendReasonRespDto> response = ApiResponse.<SuspendReasonRespDto>builder()
                .message("Suspend reason retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(suspendReason)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @PutMapping("/suspend-reasons/{id}")
    public ResponseEntity<ApiResponse<SuspendReasonRespDto>> updateSuspendReason(@PathVariable String id, @RequestBody SuspendReasonRequestDto request) {
        log.info("Updating suspend reason with id: {}", id);
        SuspendReasonRespDto suspendReason = suspendReasonService.updateSuspendReason(id, request);
        ApiResponse<SuspendReasonRespDto> response = ApiResponse.<SuspendReasonRespDto>builder()
                .message("Suspend reason updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(suspendReason)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/suspend-reasons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSuspendReason(@PathVariable String id) {
        log.info("Deleting suspend reason with id: {}", id);
        suspendReasonService.deleteSuspendReason(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Suspend reason deleted successfully")
                .statusCode(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @Override
    @PutMapping("/suspend-reasons/{id}/status")
    public ResponseEntity<ApiResponse<SuspendReasonRespDto>> updateSuspendReasonStatus(
            @PathVariable String id, 
            @RequestParam Boolean active) {
        log.info("Updating suspend reason status with id: {} to active: {}", id, active);
        SuspendReasonRespDto suspendReason = suspendReasonService.updateSuspendReasonStatus(id, active);
        ApiResponse<SuspendReasonRespDto> response = ApiResponse.<SuspendReasonRespDto>builder()
                .message("Suspend reason status updated successfully")
                .statusCode(HttpStatus.OK.value())
                .data(suspendReason)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
