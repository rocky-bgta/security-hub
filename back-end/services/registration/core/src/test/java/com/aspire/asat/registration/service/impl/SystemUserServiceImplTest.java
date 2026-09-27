package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.data.dto.AspireUserCreateRequestDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.systemUser.request.SystemUserRequestDTO;
import com.aspire.asat.registration.data.systemUser.response.SystemUserResponseDTO;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.service.AspireUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceImplTest {

    private static final String ROLE_ID = "role-123";
    private static final String EMAIL = "system.user@example.com";

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AspireUserService aspireUserService;
    @Mock private RegistrationNotificationClient registrationNotificationClient;
    @Mock private RoleRepository roleRepository;
    @Mock private AspireUserRepository aspireUserRepository;

    @InjectMocks
    private SystemUserServiceImpl systemUserService;

    @Test
    void createSystemUser_sendsWelcomeEmailNotification() {
        SystemUserRequestDTO request = buildRequest();
        UUID userId = UUID.randomUUID();
        AspireUserDto createdUser = AspireUserDto.builder()
                .baseUserId(userId)
                .email(EMAIL)
                .status("ACTIVE")
                .riskGroup(RiskGroup.HIGH_RISK)
                .createdAt(Instant.now())
                .build();

        when(aspireUserRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.existsById(ROLE_ID)).thenReturn(true);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(Role.builder()
                .id(ROLE_ID)
                .roleName("ASPIRE_ADMIN")
                .build()));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(aspireUserService.createUser(any(AspireUserCreateRequestDto.class))).thenReturn(createdUser);

        SystemUserResponseDTO response = systemUserService.createSystemUser(request);

        assertNotNull(response);
        assertEquals(EMAIL, response.getEmail());
        assertEquals("John Doe", response.getFirstName() + " " + response.getLastName());

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(registrationNotificationClient).sendWelcomeEmailNotification(
                eq(EMAIL),
                anyString(),
                isNull(),
                eq("John Doe"),
                passwordCaptor.capture()
        );
        assertNotNull(passwordCaptor.getValue());
    }

    @Test
    void createSystemUser_doesNotSendEmailWhenRoleInvalid() {
        SystemUserRequestDTO request = buildRequest();

        when(aspireUserRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.existsById(ROLE_ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> systemUserService.createSystemUser(request));

        verify(aspireUserService, never()).createUser(any(AspireUserCreateRequestDto.class));
        verify(registrationNotificationClient, never()).sendWelcomeEmailNotification(
                anyString(), anyString(), isNull(), anyString(), anyString());
    }

    private SystemUserRequestDTO buildRequest() {
        SystemUserRequestDTO request = new SystemUserRequestDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail(EMAIL);
        request.setCompanyName("Aspire Tech");
        request.setRoleIds(List.of(ROLE_ID));
        return request;
    }
}
