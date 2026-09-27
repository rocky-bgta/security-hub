package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.SubIndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.SubIndustryRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.SubIndustry;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
import com.aspire.asat.registration.repository.dropdown.SubIndustryRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubIndustryServiceImplTest {

    private static final String ORG_TYPE_ID = "org-type-1";
    private static final String INDUSTRY_ID = "industry-1";

    @Mock
    private SubIndustryRepository subIndustryRepository;

    @Mock
    private IndustryRepository industryRepository;

    @Mock
    private OrganizationTypeRepository organizationTypeRepository;

    @InjectMocks
    private SubIndustryServiceImpl subIndustryService;

    @Test
    void getSubIndustriesShouldReturnAllWhenNoFilters() {
        SubIndustry active = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "A1", "Alpha", true);
        SubIndustry inactive = subIndustry("2", ORG_TYPE_ID, INDUSTRY_ID, "B2", "Beta", false);
        when(subIndustryRepository.findWithFilters("", null, null)).thenReturn(List.of(active, inactive));

        List<SubIndustryRespDto> result = subIndustryService.getSubIndustries(null, null, null);

        assertEquals(2, result.size());
        verify(subIndustryRepository).findWithFilters("", null, null);
    }

    @Test
    void getSubIndustriesShouldFilterByActiveAndSearch() {
        SubIndustry match = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "FIN-01", "Financial Services", true);
        when(subIndustryRepository.findWithFilters(eq("fin"), eq(true), eq(null))).thenReturn(List.of(match));

        List<SubIndustryRespDto> result = subIndustryService.getSubIndustries("  fin  ", true, null);

        assertEquals(1, result.size());
        assertEquals("FIN-01", result.get(0).getCode());
        assertTrue(result.get(0).getActive());
        verify(subIndustryRepository).findWithFilters("fin", true, null);
    }

    @Test
    void getSubIndustriesShouldMapInactiveRecords() {
        SubIndustry inactive = subIndustry("3", ORG_TYPE_ID, INDUSTRY_ID, "X9", "Legacy", false);
        when(subIndustryRepository.findWithFilters("", false, null)).thenReturn(List.of(inactive));

        List<SubIndustryRespDto> result = subIndustryService.getSubIndustries(null, false, null);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getActive());
        verify(subIndustryRepository).findWithFilters("", false, null);
    }

    @Test
    void getSubIndustriesShouldForwardIndustryId() {
        SubIndustry match = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "A1", "Alpha", true);
        when(subIndustryRepository.findWithFilters("", null, INDUSTRY_ID)).thenReturn(List.of(match));

        List<SubIndustryRespDto> result = subIndustryService.getSubIndustries(null, null, INDUSTRY_ID);

        assertEquals(1, result.size());
        assertEquals(INDUSTRY_ID, result.get(0).getIndustryId());
        verify(subIndustryRepository).findWithFilters("", null, INDUSTRY_ID);
    }

    @Test
    void getSubIndustriesShouldTreatBlankIndustryIdAsNull() {
        when(subIndustryRepository.findWithFilters("", null, null)).thenReturn(List.of());

        subIndustryService.getSubIndustries(null, null, "   ");

        verify(subIndustryRepository).findWithFilters("", null, null);
    }

    @Test
    void getActiveSubIndustriesShouldReturnAllWhenNoFilters() {
        SubIndustry active = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "A1", "Alpha", true);
        when(subIndustryRepository.findByActiveTrue()).thenReturn(List.of(active));

        List<SubIndustryRespDto> result = subIndustryService.getActiveSubIndustries(null, null);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(subIndustryRepository).findByActiveTrue();
    }

    @Test
    void getActiveSubIndustriesShouldFilterByOrganizationTypeId() {
        SubIndustry match = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "A1", "Alpha", true);
        when(subIndustryRepository.findByActiveTrueAndOrganizationTypeId(ORG_TYPE_ID)).thenReturn(List.of(match));

        List<SubIndustryRespDto> result = subIndustryService.getActiveSubIndustries(ORG_TYPE_ID, null);

        assertEquals(1, result.size());
        assertEquals(ORG_TYPE_ID, result.get(0).getOrganizationTypeId());
        verify(subIndustryRepository).findByActiveTrueAndOrganizationTypeId(ORG_TYPE_ID);
    }

    @Test
    void getActiveSubIndustriesShouldFilterByIndustryId() {
        SubIndustry match = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "A1", "Alpha", true);
        when(subIndustryRepository.findByActiveTrueAndIndustryId(INDUSTRY_ID)).thenReturn(List.of(match));

        List<SubIndustryRespDto> result = subIndustryService.getActiveSubIndustries(null, INDUSTRY_ID);

        assertEquals(1, result.size());
        assertEquals(INDUSTRY_ID, result.get(0).getIndustryId());
        verify(subIndustryRepository).findByActiveTrueAndIndustryId(INDUSTRY_ID);
    }

    @Test
    void getActiveSubIndustriesShouldFilterByOrganizationTypeIdAndIndustryId() {
        SubIndustry match = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "A1", "Alpha", true);
        when(subIndustryRepository.findByActiveTrueAndOrganizationTypeIdAndIndustryId(ORG_TYPE_ID, INDUSTRY_ID))
                .thenReturn(List.of(match));

        List<SubIndustryRespDto> result = subIndustryService.getActiveSubIndustries(ORG_TYPE_ID, INDUSTRY_ID);

        assertEquals(1, result.size());
        assertEquals(ORG_TYPE_ID, result.get(0).getOrganizationTypeId());
        assertEquals(INDUSTRY_ID, result.get(0).getIndustryId());
        verify(subIndustryRepository).findByActiveTrueAndOrganizationTypeIdAndIndustryId(ORG_TYPE_ID, INDUSTRY_ID);
    }

    @Test
    void getActiveSubIndustriesShouldTreatBlankFiltersAsNull() {
        when(subIndustryRepository.findByActiveTrue()).thenReturn(List.of());

        subIndustryService.getActiveSubIndustries("   ", "  ");

        verify(subIndustryRepository).findByActiveTrue();
    }

    @Test
    void createSubIndustryShouldPersistOrganizationTypeAndIndustryId() {
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("FIN-01")
                .name("Financial Services")
                .active(true)
                .build();
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsById(INDUSTRY_ID)).thenReturn(true);
        when(subIndustryRepository.existsByIndustryIdAndCodeIgnoreCase(INDUSTRY_ID, "FIN-01")).thenReturn(false);
        when(subIndustryRepository.save(any(SubIndustry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubIndustryRespDto result = subIndustryService.createSubIndustry(request);

        assertEquals(ORG_TYPE_ID, result.getOrganizationTypeId());
        assertEquals(INDUSTRY_ID, result.getIndustryId());
        assertEquals("FIN-01", result.getCode());
        ArgumentCaptor<SubIndustry> captor = ArgumentCaptor.forClass(SubIndustry.class);
        verify(subIndustryRepository).save(captor.capture());
        assertEquals(ORG_TYPE_ID, captor.getValue().getOrganizationTypeId());
        assertEquals(INDUSTRY_ID, captor.getValue().getIndustryId());
    }

    @Test
    void createSubIndustryShouldThrowWhenOrganizationTypeMissing() {
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId("missing-org-type")
                .industryId(INDUSTRY_ID)
                .code("FIN-01")
                .name("Financial Services")
                .build();
        when(organizationTypeRepository.existsById("missing-org-type")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> subIndustryService.createSubIndustry(request));
        verify(subIndustryRepository, never()).save(any());
    }

    @Test
    void createSubIndustryShouldThrowWhenIndustryMissing() {
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId("missing-industry")
                .code("FIN-01")
                .name("Financial Services")
                .build();
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsById("missing-industry")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> subIndustryService.createSubIndustry(request));
        verify(subIndustryRepository, never()).save(any());
    }

    @Test
    void createSubIndustryShouldThrowWhenDuplicateCodeInSameIndustry() {
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("FIN-01")
                .name("Financial Services")
                .build();
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsById(INDUSTRY_ID)).thenReturn(true);
        when(subIndustryRepository.existsByIndustryIdAndCodeIgnoreCase(INDUSTRY_ID, "FIN-01")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> subIndustryService.createSubIndustry(request));
        verify(subIndustryRepository, never()).save(any());
    }

    @Test
    void createSubIndustryShouldAllowSameCodeInDifferentIndustry() {
        String otherIndustryId = "industry-2";
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(otherIndustryId)
                .code("FIN-01")
                .name("Financial Services")
                .active(true)
                .build();
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsById(otherIndustryId)).thenReturn(true);
        when(subIndustryRepository.existsByIndustryIdAndCodeIgnoreCase(otherIndustryId, "FIN-01")).thenReturn(false);
        when(subIndustryRepository.save(any(SubIndustry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubIndustryRespDto result = subIndustryService.createSubIndustry(request);

        assertEquals(otherIndustryId, result.getIndustryId());
        assertEquals("FIN-01", result.getCode());
    }

    @Test
    void updateSubIndustryShouldSetOrganizationTypeAndIndustryId() {
        SubIndustry existing = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "OLD", "Old Name", true);
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId("org-type-2")
                .industryId("industry-2")
                .code("NEW")
                .name("New Name")
                .active(false)
                .build();
        when(subIndustryRepository.findById("1")).thenReturn(Optional.of(existing));
        when(organizationTypeRepository.existsById("org-type-2")).thenReturn(true);
        when(industryRepository.existsById("industry-2")).thenReturn(true);
        when(subIndustryRepository.existsByIndustryIdAndCodeIgnoreCaseAndIdNot("industry-2", "NEW", "1"))
                .thenReturn(false);
        when(subIndustryRepository.save(any(SubIndustry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubIndustryRespDto result = subIndustryService.updateSubIndustry("1", request);

        assertEquals("org-type-2", result.getOrganizationTypeId());
        assertEquals("industry-2", result.getIndustryId());
        assertEquals("NEW", result.getCode());
        assertEquals("New Name", result.getName());
        assertFalse(result.getActive());
    }

    @Test
    void updateSubIndustryShouldThrowWhenDuplicateCodeInTargetIndustry() {
        SubIndustry existing = subIndustry("1", ORG_TYPE_ID, INDUSTRY_ID, "OLD", "Old Name", true);
        SubIndustryRequestDto request = SubIndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .industryId(INDUSTRY_ID)
                .code("DUP")
                .name("Duplicate")
                .build();
        when(subIndustryRepository.findById("1")).thenReturn(Optional.of(existing));
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsById(INDUSTRY_ID)).thenReturn(true);
        when(subIndustryRepository.existsByIndustryIdAndCodeIgnoreCaseAndIdNot(INDUSTRY_ID, "DUP", "1"))
                .thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> subIndustryService.updateSubIndustry("1", request));
        verify(subIndustryRepository, never()).save(any());
    }

    @Test
    void getSubIndustryByIdShouldThrowWhenMissing() {
        when(subIndustryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subIndustryService.getSubIndustryById("missing"));
    }

    @Test
    void deleteSubIndustryShouldThrowWhenMissing() {
        when(subIndustryRepository.existsById("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> subIndustryService.deleteSubIndustry("missing"));
        verify(subIndustryRepository, never()).deleteById(any());
    }

    private static SubIndustry subIndustry(String id, String organizationTypeId, String industryId,
                                           String code, String name, boolean active) {
        return SubIndustry.builder()
                .id(id)
                .organizationTypeId(organizationTypeId)
                .industryId(industryId)
                .code(code)
                .name(name)
                .active(active)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();
    }
}
