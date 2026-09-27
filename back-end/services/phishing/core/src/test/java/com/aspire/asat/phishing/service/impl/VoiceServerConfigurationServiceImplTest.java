package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.dto.enums.VoiceServerStatus;
import com.aspire.asat.phishing.dto.request.VoiceServerConfigurationRequest;
import com.aspire.asat.phishing.dto.response.VoiceServerConfigurationDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.VoiceServerConfigurationMapper;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.repository.VoiceServerConfigurationRepository;
import com.aspire.asat.phishing.service.ConfigurationAuditService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.aspire.asat.phishing.voice.VoiceProviderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
class VoiceServerConfigurationServiceImplTest {

    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    @Mock
    private VoiceServerConfigurationRepository repository;
    @Mock
    private VoiceServerConfigurationMapper mapper;
    @Mock
    private CredentialEncryptionService credentialEncryptionService;
    @Mock
    private ConfigurationAuditService configurationAuditService;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private VoiceProviderFactory voiceProviderFactory;

    @InjectMocks
    private VoiceServerConfigurationServiceImpl service;

    @Test
    void create_platformAdmin_setsGlobalTrue() {
        stubUser(CLIENT_A, UserType.ASPIRE_ADMIN);
        VoiceServerConfigurationRequest request = baseCreateRequest("Platform Twilio");
        VoiceServerConfiguration entity = VoiceServerConfiguration.builder()
                .clientId(CLIENT_A)
                .name("Platform Twilio")
                .provider(VoiceProviderType.TWILIO)
                .build();
        VoiceServerConfiguration saved = VoiceServerConfiguration.builder()
                .id("vs-1")
                .clientId(CLIENT_A)
                .name("Platform Twilio")
                .provider(VoiceProviderType.TWILIO)
                .isGlobal(true)
                .build();

        when(repository.existsByIsGlobalTrueAndName("Platform Twilio")).thenReturn(false);
        when(mapper.toEntity(request, CLIENT_A)).thenReturn(entity);
        when(credentialEncryptionService.encrypt(any())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        when(repository.save(any())).thenReturn(saved);
        when(mapper.toDto(saved, true)).thenReturn(VoiceServerConfigurationDto.builder()
                .id("vs-1").isGlobal(true).canEdit(true).build());

        VoiceServerConfigurationDto dto = service.create(request);

        assertTrue(dto.isGlobal());
        ArgumentCaptor<VoiceServerConfiguration> captor = ArgumentCaptor.forClass(VoiceServerConfiguration.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().isGlobal());
    }

    @Test
    void create_clientAdmin_setsGlobalFalse() {
        stubUser(CLIENT_A, UserType.CLIENT_ADMIN);
        VoiceServerConfigurationRequest request = baseCreateRequest("Client Twilio");
        VoiceServerConfiguration entity = VoiceServerConfiguration.builder()
                .clientId(CLIENT_A)
                .name("Client Twilio")
                .provider(VoiceProviderType.TWILIO)
                .build();
        VoiceServerConfiguration saved = VoiceServerConfiguration.builder()
                .id("vs-2")
                .clientId(CLIENT_A)
                .name("Client Twilio")
                .isGlobal(false)
                .build();

        when(repository.existsByClientIdAndName(CLIENT_A, "Client Twilio")).thenReturn(false);
        when(mapper.toEntity(request, CLIENT_A)).thenReturn(entity);
        when(credentialEncryptionService.encrypt(any())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        when(repository.save(any())).thenReturn(saved);
        when(mapper.toDto(saved, true)).thenReturn(VoiceServerConfigurationDto.builder()
                .id("vs-2").isGlobal(false).canEdit(true).build());

        VoiceServerConfigurationDto dto = service.create(request);

        assertFalse(dto.isGlobal());
        ArgumentCaptor<VoiceServerConfiguration> captor = ArgumentCaptor.forClass(VoiceServerConfiguration.class);
        verify(repository).save(captor.capture());
        assertFalse(captor.getValue().isGlobal());
    }

    @Test
    void list_client_usesClientIdOrGlobal() {
        stubUser(CLIENT_A, UserType.CLIENT_ADMIN);
        VoiceServerConfiguration own = VoiceServerConfiguration.builder()
                .id("own").clientId(CLIENT_A).isGlobal(false).build();
        VoiceServerConfiguration global = VoiceServerConfiguration.builder()
                .id("global").clientId("platform").isGlobal(true).build();
        Page<VoiceServerConfiguration> page = new PageImpl<>(List.of(own, global));

        when(repository.findByClientIdOrGlobal(eq(CLIENT_A), any(Pageable.class))).thenReturn(page);
        when(mapper.toDto(eq(own), eq(true))).thenReturn(
                VoiceServerConfigurationDto.builder().id("own").canEdit(true).build());
        when(mapper.toDto(eq(global), eq(false))).thenReturn(
                VoiceServerConfigurationDto.builder().id("global").canEdit(false).isGlobal(true).build());

        List<VoiceServerConfigurationDto> result = service.getConfigurations(0, 10, null);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isCanEdit());
        assertFalse(result.get(1).isCanEdit());
        verify(repository, never()).findByClientId(any(), any());
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void list_platformAdmin_listsAllWhenNoClientFilter() {
        stubUser(CLIENT_A, UserType.SUPER_ADMIN);
        VoiceServerConfiguration other = VoiceServerConfiguration.builder()
                .id("other").clientId(CLIENT_B).isGlobal(false).build();
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(other)));
        when(mapper.toDto(eq(other), eq(true))).thenReturn(
                VoiceServerConfigurationDto.builder().id("other").canEdit(true).build());

        List<VoiceServerConfigurationDto> result = service.getConfigurations(0, 10, null);

        assertEquals(1, result.size());
        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    void update_client_deniedOnGlobal() {
        stubUser(CLIENT_A, UserType.CLIENT_ADMIN);
        VoiceServerConfiguration global = VoiceServerConfiguration.builder()
                .id("global")
                .clientId("platform")
                .name("Global")
                .isGlobal(true)
                .status(VoiceServerStatus.ACTIVE)
                .build();
        when(repository.findByIdAndClientIdOrGlobal("global", CLIENT_A)).thenReturn(Optional.of(global));

        VoiceServerConfigurationRequest request = VoiceServerConfigurationRequest.builder()
                .name("Global")
                .provider(VoiceProviderType.TWILIO)
                .build();

        assertThrows(PhishingValidationException.class, () -> service.update("global", request));
        verify(repository, never()).save(any());
    }

    @Test
    void update_client_allowedOnOwn() {
        stubUser(CLIENT_A, UserType.CLIENT_ADMIN);
        VoiceServerConfiguration own = VoiceServerConfiguration.builder()
                .id("own")
                .clientId(CLIENT_A)
                .name("Own")
                .isGlobal(false)
                .status(VoiceServerStatus.ACTIVE)
                .build();
        when(repository.findByIdAndClientIdOrGlobal("own", CLIENT_A)).thenReturn(Optional.of(own));
        when(repository.existsByClientIdAndNameAndIdNot(CLIENT_A, "Own Updated", "own")).thenReturn(false);
        when(repository.save(own)).thenReturn(own);
        when(mapper.toDto(own, true)).thenReturn(
                VoiceServerConfigurationDto.builder().id("own").canEdit(true).build());

        VoiceServerConfigurationRequest request = VoiceServerConfigurationRequest.builder()
                .name("Own Updated")
                .provider(VoiceProviderType.TWILIO)
                .build();

        service.update("own", request);

        verify(mapper).applyUpdate(own, request);
        verify(repository).save(own);
    }

    @Test
    void delete_platformAdmin_canDeleteOtherClientServer() {
        stubUser(CLIENT_A, UserType.SYSTEM_USER);
        VoiceServerConfiguration other = VoiceServerConfiguration.builder()
                .id("other")
                .clientId(CLIENT_B)
                .name("Other")
                .isGlobal(false)
                .build();
        when(repository.findById("other")).thenReturn(Optional.of(other));

        service.delete("other");

        verify(repository).delete(other);
    }

    @Test
    void resolveForCampaign_acceptsGlobalForClient() {
        VoiceServerConfiguration global = VoiceServerConfiguration.builder()
                .id("global")
                .clientId("platform")
                .isGlobal(true)
                .status(VoiceServerStatus.ACTIVE)
                .build();
        when(repository.findByIdAndClientIdOrGlobal("global", CLIENT_A)).thenReturn(Optional.of(global));

        VoiceServerConfiguration resolved = service.resolveForCampaign(CLIENT_A, "global");

        assertEquals("global", resolved.getId());
        assertTrue(resolved.isGlobal());
    }

    private void stubUser(String clientId, UserType userType) {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .clientAdminId(clientId)
                .userId("user-1")
                .userType(userType.getValue())
                .build());
    }

    private static VoiceServerConfigurationRequest baseCreateRequest(String name) {
        return VoiceServerConfigurationRequest.builder()
                .name(name)
                .provider(VoiceProviderType.TWILIO)
                .apiKey("ACxxxx")
                .apiSecret("token")
                .callerId("+15551234567")
                .build();
    }
}
