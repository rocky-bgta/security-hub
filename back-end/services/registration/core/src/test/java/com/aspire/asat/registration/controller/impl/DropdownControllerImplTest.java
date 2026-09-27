package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.dropdown.IndustryRespDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRespDto;
import com.aspire.asat.registration.service.dropdown.CountryService;
import com.aspire.asat.registration.service.dropdown.IndustryService;
import com.aspire.asat.registration.service.dropdown.LanguageService;
import com.aspire.asat.registration.service.dropdown.MspTypeService;
import com.aspire.asat.registration.service.dropdown.OrganizationSizeService;
import com.aspire.asat.registration.service.dropdown.StateService;
import com.aspire.asat.registration.service.dropdown.SubIndustryService;
import com.aspire.asat.registration.service.dropdown.SuspendReasonService;
import com.aspire.asat.registration.service.dropdown.TimezoneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropdownControllerImplTest {

    private static final String ORG_TYPE_ID = "org-type-1";
    private static final String INDUSTRY_ID = "industry-1";
    private static final String SUB_INDUSTRY_ID = "sub-industry-1";

    @Mock
    private CountryService countryService;
    @Mock
    private StateService stateService;
    @Mock
    private TimezoneService timezoneService;
    @Mock
    private LanguageService languageService;
    @Mock
    private IndustryService industryService;
    @Mock
    private SubIndustryService subIndustryService;
    @Mock
    private OrganizationSizeService organizationSizeService;
    @Mock
    private MspTypeService mspTypeService;
    @Mock
    private SuspendReasonService suspendReasonService;

    @InjectMocks
    private DropdownControllerImpl controller;

    @Test
    void getActiveIndustries_forwardsOrganizationTypeIdToService() {
        IndustryRespDto dto = sampleIndustry();
        when(industryService.getActiveIndustries(ORG_TYPE_ID)).thenReturn(List.of(dto));

        ResponseEntity<ApiResponse<List<IndustryRespDto>>> response =
                controller.getActiveIndustries(ORG_TYPE_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(ORG_TYPE_ID, response.getBody().getData().get(0).getOrganizationTypeId());
        verify(industryService).getActiveIndustries(ORG_TYPE_ID);
    }

    @Test
    void getActiveIndustries_omittedOrganizationTypeIdCallsServiceWithNull() {
        when(industryService.getActiveIndustries(null)).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<IndustryRespDto>>> response =
                controller.getActiveIndustries(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(industryService).getActiveIndustries(null);
    }

    @Test
    void getSubIndustries_forwardsIndustryIdToService() {
        SubIndustryRespDto dto = sampleSubIndustry();
        when(subIndustryService.getSubIndustries("fin", true, INDUSTRY_ID)).thenReturn(List.of(dto));

        ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> response =
                controller.getSubIndustries("fin", true, INDUSTRY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Sub-industries retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(INDUSTRY_ID, response.getBody().getData().get(0).getIndustryId());
        verify(subIndustryService).getSubIndustries("fin", true, INDUSTRY_ID);
    }

    @Test
    void getSubIndustries_omittedIndustryIdCallsServiceWithNull() {
        when(subIndustryService.getSubIndustries(null, null, null)).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> response =
                controller.getSubIndustries(null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getData().size());
        verify(subIndustryService).getSubIndustries(null, null, null);
    }

    @Test
    void createSubIndustry_returnsCreatedEnvelopeWithIndustryId() {
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("FIN-01")
                .name("Financial Services")
                .active(true)
                .build();
        when(subIndustryService.createSubIndustry(request)).thenReturn(sampleSubIndustry());

        ResponseEntity<ApiResponse<SubIndustryRespDto>> response = controller.createSubIndustry(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatusCode());
        assertEquals("Sub-industry created successfully", response.getBody().getMessage());
        assertEquals(ORG_TYPE_ID, response.getBody().getData().getOrganizationTypeId());
        assertEquals(INDUSTRY_ID, response.getBody().getData().getIndustryId());
        verify(subIndustryService).createSubIndustry(request);
    }

    @Test
    void getSubIndustryById_returnsOkEnvelope() {
        when(subIndustryService.getSubIndustryById(SUB_INDUSTRY_ID)).thenReturn(sampleSubIndustry());

        ResponseEntity<ApiResponse<SubIndustryRespDto>> response =
                controller.getSubIndustryById(SUB_INDUSTRY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sub-industry retrieved successfully", response.getBody().getMessage());
        assertEquals(SUB_INDUSTRY_ID, response.getBody().getData().getId());
        assertEquals(INDUSTRY_ID, response.getBody().getData().getIndustryId());
        verify(subIndustryService).getSubIndustryById(SUB_INDUSTRY_ID);
    }

    @Test
    void updateSubIndustry_returnsOkEnvelope() {
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("FIN-02")
                .name("Updated")
                .active(false)
                .build();
        SubIndustryRespDto updated = SubIndustryRespDto.builder()
                .id(SUB_INDUSTRY_ID)
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("FIN-02")
                .name("Updated")
                .active(false)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-03T00:00:00Z"))
                .build();
        when(subIndustryService.updateSubIndustry(SUB_INDUSTRY_ID, request)).thenReturn(updated);

        ResponseEntity<ApiResponse<SubIndustryRespDto>> response =
                controller.updateSubIndustry(SUB_INDUSTRY_ID, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sub-industry updated successfully", response.getBody().getMessage());
        assertEquals("FIN-02", response.getBody().getData().getCode());
        verify(subIndustryService).updateSubIndustry(SUB_INDUSTRY_ID, request);
    }

    @Test
    void getActiveSubIndustries_forwardsFiltersToService() {
        when(subIndustryService.getActiveSubIndustries(ORG_TYPE_ID, INDUSTRY_ID))
                .thenReturn(List.of(sampleSubIndustry()));

        ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> response =
                controller.getActiveSubIndustries(ORG_TYPE_ID, INDUSTRY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Active sub-industries retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        verify(subIndustryService).getActiveSubIndustries(ORG_TYPE_ID, INDUSTRY_ID);
    }

    @Test
    void getActiveSubIndustries_omittedFiltersCallServiceWithNull() {
        when(subIndustryService.getActiveSubIndustries(null, null)).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<SubIndustryRespDto>>> response =
                controller.getActiveSubIndustries(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(subIndustryService).getActiveSubIndustries(null, null);
    }

    @Test
    void deleteSubIndustry_returnsNoContentEnvelope() {
        ResponseEntity<ApiResponse<Void>> response = controller.deleteSubIndustry(SUB_INDUSTRY_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(204, response.getBody().getStatusCode());
        assertEquals("Sub-industry deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(subIndustryService).deleteSubIndustry(SUB_INDUSTRY_ID);
    }

    private static IndustryRespDto sampleIndustry() {
        return IndustryRespDto.builder()
                .id(INDUSTRY_ID)
                .organizationTypeId(ORG_TYPE_ID)
                .code("IT")
                .name("Information Technology")
                .active(true)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();
    }

    private static SubIndustryRespDto sampleSubIndustry() {
        return SubIndustryRespDto.builder()
                .id(SUB_INDUSTRY_ID)
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("FIN-01")
                .name("Financial Services")
                .active(true)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();
    }
}
