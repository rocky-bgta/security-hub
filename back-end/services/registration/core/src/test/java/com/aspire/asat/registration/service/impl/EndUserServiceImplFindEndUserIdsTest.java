package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.department.response.DepartmentResponseDTO;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserServiceImplFindEndUserIdsTest {

    @Mock
    private AspireUserRepository aspireUserRepository;
    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private EndUserServiceImpl endUserService;

    @Test
    void findEndUserIds_blankClientAdminIds_throwsValidation() {
        assertThrows(RegistationValidationException.class,
                () -> endUserService.findEndUserIds(List.of("  "), "alice", List.of("HR")));
        verifyNoInteractions(aspireUserRepository);
    }

    @Test
    void findEndUserIds_searchAndDepartment_delegatesToRepository() {
        when(departmentService.getDepartmentById("HR")).thenReturn(Optional.empty());
        when(aspireUserRepository.findEndUserIds(eq(List.of("client-1")), eq("alice"), eq(List.of("HR"))))
                .thenReturn(List.of("user-1"));

        List<String> ids = endUserService.findEndUserIds(List.of("client-1"), "alice", List.of("HR"));

        assertEquals(List.of("user-1"), ids);
        verify(aspireUserRepository).findEndUserIds(List.of("client-1"), "alice", List.of("HR"));
    }

    @Test
    void findEndUserIds_emptyMatch_returnsEmptyList() {
        when(departmentService.getDepartmentById("Finance")).thenReturn(Optional.empty());
        when(aspireUserRepository.findEndUserIds(eq(List.of("client-1")), isNull(), eq(List.of("Finance"))))
                .thenReturn(List.of());

        List<String> ids = endUserService.findEndUserIds(List.of("client-1"), null, List.of("Finance"));

        assertEquals(List.of(), ids);
    }

    @Test
    void findEndUserIds_departmentId_alsoMatchesDepartmentName() {
        when(departmentService.getDepartmentById("dept-1"))
                .thenReturn(Optional.of(DepartmentResponseDTO.builder()
                        .id("dept-1")
                        .name("Audit/Internal Controls")
                        .build()));
        when(aspireUserRepository.findEndUserIds(
                eq(List.of("client-1")), isNull(), eq(List.of("dept-1", "Audit/Internal Controls"))))
                .thenReturn(List.of("user-1"));

        List<String> ids = endUserService.findEndUserIds(List.of("client-1"), null, List.of("dept-1"));

        assertEquals(List.of("user-1"), ids);
    }
}
