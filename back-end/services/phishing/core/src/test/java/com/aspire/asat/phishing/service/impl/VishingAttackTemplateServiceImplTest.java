package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingAttackTemplateDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.VishingAttackTemplateMapper;
import com.aspire.asat.phishing.model.VishingAttackTemplate;
import com.aspire.asat.phishing.repository.VishingAttackTemplateRepository;
import com.aspire.asat.phishing.util.VishingScriptPlaceholderUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
class VishingAttackTemplateServiceImplTest {

    @Mock
    private VishingAttackTemplateRepository repository;

    @Spy
    private VishingAttackTemplateMapper mapper = new VishingAttackTemplateMapper();

    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private VishingAttackTemplateServiceImpl service;

    @Test
    void createShouldDetectVariablesAndReturnDto() {
        stubUser(UserType.ASPIRE_ADMIN);
        VishingAttackTemplateCreateRequest request = VishingAttackTemplateCreateRequest.builder()
                .name("Bank Fraud Alert")
                .script("Hello {{FIRST_NAME}}, your OTP is {{OTP}}")
                .build();

        when(repository.existsByNameIgnoreCase("Bank Fraud Alert")).thenReturn(false);
        when(repository.save(any(VishingAttackTemplate.class))).thenAnswer(inv -> {
            VishingAttackTemplate entity = inv.getArgument(0);
            entity.setId("tpl-1");
            return entity;
        });

        VishingAttackTemplateDto result = service.create(request);

        assertEquals("tpl-1", result.getId());
        assertEquals("Bank Fraud Alert", result.getName());
        assertEquals(List.of("{{FIRST_NAME}}", "{{OTP}}"), result.getVariables());
        verify(repository).save(any(VishingAttackTemplate.class));
    }

    @Test
    void createShouldRejectDuplicateName() {
        stubUser(UserType.SUPER_ADMIN);
        when(repository.existsByNameIgnoreCase("Bank Fraud Alert")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> service.create(
                VishingAttackTemplateCreateRequest.builder()
                        .name("Bank Fraud Alert")
                        .script("Hello")
                        .build()));
        verify(repository, never()).save(any());
    }

    @Test
    void createShouldDenyClientAdmin() {
        stubUser(UserType.CLIENT_ADMIN);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(
                VishingAttackTemplateCreateRequest.builder()
                        .name("Bank Fraud Alert")
                        .script("Hello {{FIRST_NAME}}")
                        .build()));

        assertTrue(ex.getMessage().contains("permission"));
        verify(repository, never()).save(any());
    }

    @Test
    void updateShouldDenyClientAdmin() {
        stubUser(UserType.CLIENT_ADMIN);

        assertThrows(ServiceException.class, () -> service.update(
                "tpl-1",
                VishingAttackTemplateUpdateRequest.builder()
                        .name("Updated")
                        .script("Hi")
                        .build()));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteShouldDenyClientAdmin() {
        stubUser(UserType.CLIENT_ADMIN);

        assertThrows(ServiceException.class, () -> service.delete("tpl-1"));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void updateShouldReDetectVariables() {
        stubUser(UserType.SYSTEM_USER);
        VishingAttackTemplate existing = VishingAttackTemplate.builder()
                .id("tpl-1")
                .name("Old")
                .script("Hello")
                .variables(List.of())
                .build();
        when(repository.findById("tpl-1")).thenReturn(Optional.of(existing));
        when(repository.existsByNameIgnoreCaseAndIdNot("New Name", "tpl-1")).thenReturn(false);
        when(repository.save(any(VishingAttackTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        VishingAttackTemplateDto result = service.update(
                "tpl-1",
                VishingAttackTemplateUpdateRequest.builder()
                        .name("New Name")
                        .script("Call {{PHONE}} now")
                        .build());

        assertEquals("New Name", result.getName());
        assertEquals(List.of("{{PHONE}}"), result.getVariables());
    }

    @Test
    void getByIdShouldReturnTemplateForAnyUser() {
        when(repository.findById("tpl-1")).thenReturn(Optional.of(
                VishingAttackTemplate.builder()
                        .id("tpl-1")
                        .name("Bank Fraud Alert")
                        .script("Hello {{FIRST_NAME}}")
                        .variables(VishingScriptPlaceholderUtils.detectVariables("Hello {{FIRST_NAME}}"))
                        .build()));

        VishingAttackTemplateDto result = service.getById("tpl-1");

        assertEquals("tpl-1", result.getId());
        assertEquals(List.of("{{FIRST_NAME}}"), result.getVariables());
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById("missing"));
    }

    private void stubUser(UserType userType) {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .clientAdminId("client-1")
                .userId("user-1")
                .userType(userType.getValue())
                .build());
    }
}
