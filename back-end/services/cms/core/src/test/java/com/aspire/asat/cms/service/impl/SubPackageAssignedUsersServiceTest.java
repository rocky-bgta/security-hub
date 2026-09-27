package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageAssignedUserDto;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubPackageAssignedUsersServiceTest {

    @Mock
    private UserSubPackageRepository userSubPackageRepository;
    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @InjectMocks
    private SubPackageServiceImpl subPackageService;

    @Test
    void getAssignedUsersBySubPackageId_enrichesWithAspireUserDetails() {
        UserSubPackage assignment = UserSubPackage.builder()
                .userId("user-1")
                .subPackageId("sub-1")
                .subPackageName("Security Awareness")
                .status("IN_PROGRESS")
                .assignedDate(LocalDate.of(2026, 7, 1))
                .expiryDate(LocalDate.of(2026, 12, 31))
                .progress(45.0)
                .build();

        when(userSubPackageRepository.findBySubPackageId(eq("sub-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(assignment)));
        when(registrationServiceClient.getUsersByIds(anyList()))
                .thenReturn(List.of(AspireUserBasicDto.builder()
                        .userId("user-1")
                        .email("john@company.com")
                        .fullName("John Doe")
                        .build()));

        Page<SubPackageAssignedUserDto> page = subPackageService.getAssignedUsersBySubPackageId("sub-1", 0, 10);

        assertEquals(1, page.getContent().size());
        SubPackageAssignedUserDto item = page.getContent().get(0);
        assertEquals("user-1", item.getUserId());
        assertEquals("john@company.com", item.getEmail());
        assertEquals("John Doe", item.getFullName());
        assertEquals("Security Awareness", item.getSubPackageName());
        assertEquals("IN_PROGRESS", item.getStatus());
        assertEquals(45.0, item.getProgress());

        verify(registrationServiceClient).getUsersByIds(List.of("user-1"));
    }
}
