package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.IndustryRequestDto;
import com.aspire.asat.registration.data.dropdown.IndustryRespDto;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.dropdown.Industry;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndustryServiceImplTest {

    private static final String ORG_TYPE_ID = "org-type-1";

    @Mock
    private IndustryRepository industryRepository;

    @Mock
    private OrganizationTypeRepository organizationTypeRepository;

    @InjectMocks
    private IndustryServiceImpl industryService;

    @Test
    void createIndustryShouldPersistFields() {
        IndustryRequestDto request = IndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .code("IT")
                .name("Information Technology")
                .active(true)
                .build();
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsByCode("IT")).thenReturn(false);
        when(industryRepository.save(any(Industry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IndustryRespDto result = industryService.createIndustry(request);

        assertEquals(ORG_TYPE_ID, result.getOrganizationTypeId());
        assertEquals("IT", result.getCode());
        assertEquals("Information Technology", result.getName());
        assertTrue(result.getActive());
        ArgumentCaptor<Industry> captor = ArgumentCaptor.forClass(Industry.class);
        verify(industryRepository).save(captor.capture());
        assertEquals(ORG_TYPE_ID, captor.getValue().getOrganizationTypeId());
        assertEquals("IT", captor.getValue().getCode());
    }

    @Test
    void createIndustryShouldThrowWhenOrganizationTypeMissing() {
        IndustryRequestDto request = IndustryRequestDto.builder()
                .organizationTypeId("missing-org-type")
                .code("IT")
                .name("Information Technology")
                .build();
        when(organizationTypeRepository.existsById("missing-org-type")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> industryService.createIndustry(request));
        verify(industryRepository, never()).save(any());
    }

    @Test
    void createIndustryShouldThrowWhenDuplicateCode() {
        IndustryRequestDto request = IndustryRequestDto.builder()
                .organizationTypeId(ORG_TYPE_ID)
                .code("IT")
                .name("Information Technology")
                .build();
        when(organizationTypeRepository.existsById(ORG_TYPE_ID)).thenReturn(true);
        when(industryRepository.existsByCode("IT")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> industryService.createIndustry(request));
        verify(industryRepository, never()).save(any());
    }

    @Test
    void getIndustryByIdShouldReturnMappedDto() {
        Industry industry = industry("1", ORG_TYPE_ID, "IT", "Information Technology", true);
        when(industryRepository.findById("1")).thenReturn(Optional.of(industry));

        IndustryRespDto result = industryService.getIndustryById("1");

        assertEquals("1", result.getId());
        assertEquals(ORG_TYPE_ID, result.getOrganizationTypeId());
        assertEquals("IT", result.getCode());
        assertEquals("Information Technology", result.getName());
    }

    @Test
    void getIndustryByIdShouldThrowWhenMissing() {
        when(industryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> industryService.getIndustryById("missing"));
    }

    @Test
    void updateIndustryShouldUpdateFields() {
        Industry existing = industry("1", ORG_TYPE_ID, "OLD", "Old Name", true);
        IndustryRequestDto request = IndustryRequestDto.builder()
                .organizationTypeId("org-type-2")
                .code("NEW")
                .name("New Name")
                .active(false)
                .build();
        when(industryRepository.findById("1")).thenReturn(Optional.of(existing));
        when(organizationTypeRepository.existsById("org-type-2")).thenReturn(true);
        when(industryRepository.existsByCode("NEW")).thenReturn(false);
        when(industryRepository.save(any(Industry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IndustryRespDto result = industryService.updateIndustry("1", request);

        assertEquals("org-type-2", result.getOrganizationTypeId());
        assertEquals("NEW", result.getCode());
        assertEquals("New Name", result.getName());
        assertEquals(false, result.getActive());
    }

    @Test
    void deleteIndustryShouldRemoveWhenExists() {
        when(industryRepository.existsById("1")).thenReturn(true);

        industryService.deleteIndustry("1");

        verify(industryRepository).deleteById("1");
    }

    @Test
    void deleteIndustryShouldThrowWhenMissing() {
        when(industryRepository.existsById("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> industryService.deleteIndustry("missing"));
        verify(industryRepository, never()).deleteById(any());
    }

    @Test
    void getActiveIndustriesShouldReturnMappedList() {
        Industry active = industry("1", ORG_TYPE_ID, "IT", "Information Technology", true);
        when(industryRepository.findByActiveTrue()).thenReturn(List.of(active));

        List<IndustryRespDto> result = industryService.getActiveIndustries(null);

        assertEquals(1, result.size());
        assertEquals("IT", result.get(0).getCode());
        assertTrue(result.get(0).getActive());
        verify(industryRepository).findByActiveTrue();
    }

    @Test
    void getActiveIndustriesShouldFilterByOrganizationTypeId() {
        Industry match = industry("1", ORG_TYPE_ID, "IT", "Information Technology", true);
        when(industryRepository.findByActiveTrueAndOrganizationTypeId(ORG_TYPE_ID)).thenReturn(List.of(match));

        List<IndustryRespDto> result = industryService.getActiveIndustries(ORG_TYPE_ID);

        assertEquals(1, result.size());
        assertEquals(ORG_TYPE_ID, result.get(0).getOrganizationTypeId());
        verify(industryRepository).findByActiveTrueAndOrganizationTypeId(ORG_TYPE_ID);
    }

    @Test
    void getActiveIndustriesShouldTreatBlankOrganizationTypeIdAsNull() {
        when(industryRepository.findByActiveTrue()).thenReturn(List.of());

        industryService.getActiveIndustries("   ");

        verify(industryRepository).findByActiveTrue();
    }

    private static Industry industry(String id, String organizationTypeId, String code, String name, boolean active) {
        return Industry.builder()
                .id(id)
                .organizationTypeId(organizationTypeId)
                .code(code)
                .name(name)
                .active(active)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();
    }
}
