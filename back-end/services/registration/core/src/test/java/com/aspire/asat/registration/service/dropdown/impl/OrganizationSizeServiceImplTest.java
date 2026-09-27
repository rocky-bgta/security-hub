package com.aspire.asat.registration.service.dropdown.impl;

import com.aspire.asat.registration.data.dropdown.OrganizationSizeRespDto;
import com.aspire.asat.registration.model.dropdown.OrganizationSize;
import com.aspire.asat.registration.repository.dropdown.OrganizationSizeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationSizeServiceImplTest {

    @Mock
    private OrganizationSizeRepository organizationSizeRepository;

    @InjectMocks
    private OrganizationSizeServiceImpl organizationSizeService;

    @Test
    void getActiveOrganizationSizes_sortsByNumericRangeStart() {
        Instant now = Instant.parse("2025-10-08T10:00:00Z");
        when(organizationSizeRepository.findAll()).thenReturn(List.of(
                size("fa3931f4-1674-4057-99ec-52ebd2c24a64", "Lower-Medium", "51-100", now),
                size("351fc334-694b-4f93-98cb-030a81d549c9", "Medium", "101–250", now),
                size("36f0f3c9-0b73-46be-a221-53567ced9d65", "Upper-Medium", "251–500", now),
                size("44bfd264-8582-4014-bedd-875bc0c8613a", "Large", "501–1000", now),
                size("7a080c69-b73e-4a60-8a4a-5e8dde7b54a4", "Enterprise", "1001–5000", now),
                size("98cd2eaa-6a97-44d9-a8db-fab76af0a17a", "Mega", "5001+", now),
                size("f9cb132a-8f35-460d-857f-abd96907d71b", "Small", "1-50", now)
        ));

        List<OrganizationSizeRespDto> result = organizationSizeService.getActiveOrganizationSizes();

        assertEquals(List.of(
                "1-50",
                "51-100",
                "101–250",
                "251–500",
                "501–1000",
                "1001–5000",
                "5001+"
        ), result.stream().map(OrganizationSizeRespDto::getRange).toList());
        assertEquals("Small", result.get(0).getName());
        assertEquals("Lower-Medium", result.get(1).getName());
        assertEquals("Mega", result.get(6).getName());
    }

    private static OrganizationSize size(String id, String name, String range, Instant now) {
        return OrganizationSize.builder()
                .id(id)
                .name(name)
                .range(range)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
