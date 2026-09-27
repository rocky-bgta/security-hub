package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.ProviderCredentialMapper;
import com.aspire.asat.phishing.model.ProviderCredential;
import com.aspire.asat.phishing.repository.ProviderCredentialRepository;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderCredentialServiceImplTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private ProviderCredentialRepository repository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    private CredentialEncryptionService encryptionService;
    private ProviderCredentialServiceImpl service;

    @BeforeEach
    void setUp() {
        encryptionService = new CredentialEncryptionService(Base64.getEncoder().encodeToString(new byte[32]));
        service = new ProviderCredentialServiceImpl(
                repository, new ProviderCredentialMapper(), encryptionService, userCurrentContextService);
        lenient().when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    @Test
    void create_encryptsSecretsAndClearsExistingDefaults() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .modelName("eleven_multilingual_v2")
                .apiKey("plain-key-6789")
                .apiSecret("plain-secret")
                .baseUrl("https://api.elevenlabs.io")
                .isActive(true)
                .isDefault(true)
                .build();
        when(repository.existsByClientIdAndProviderName(CLIENT_ID, "ELEVENLABS")).thenReturn(false);
        when(repository.findByClientIdAndCategory(CLIENT_ID, ProviderCategory.VOICE_CLONING))
                .thenReturn(List.of());
        when(repository.save(any(ProviderCredential.class))).thenAnswer(inv -> {
            ProviderCredential e = inv.getArgument(0);
            e.setId("pc-1");
            return e;
        });

        ProviderCredentialDto dto = service.createProviderCredential(request);

        ArgumentCaptor<ProviderCredential> captor = ArgumentCaptor.forClass(ProviderCredential.class);
        verify(repository).save(captor.capture());
        ProviderCredential saved = captor.getValue();
        assertTrue(encryptionService.isEncrypted(saved.getApiKey()));
        assertTrue(encryptionService.isEncrypted(saved.getApiSecret()));
        assertEquals("plain-key-6789", encryptionService.decrypt(saved.getApiKey()));
        assertEquals("eleven_multilingual_v2", saved.getModelName());
        assertEquals("6789", dto.getApiKeyLast4());
        assertEquals("eleven_multilingual_v2", dto.getModelName());
        assertTrue(dto.getHasApiSecret());
        assertTrue(dto.getIsDefault());
    }

    @Test
    void create_storesTypedProviderNameUnchanged() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("My TTS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("k")
                .baseUrl("https://api.elevenlabs.io")
                .isActive(true)
                .isDefault(false)
                .build();
        when(repository.existsByClientIdAndProviderName(CLIENT_ID, "My TTS")).thenReturn(false);
        when(repository.save(any(ProviderCredential.class))).thenAnswer(inv -> {
            ProviderCredential e = inv.getArgument(0);
            e.setId("pc-custom");
            return e;
        });

        ProviderCredentialDto dto = service.createProviderCredential(request);

        ArgumentCaptor<ProviderCredential> captor = ArgumentCaptor.forClass(ProviderCredential.class);
        verify(repository).save(captor.capture());
        assertEquals("My TTS", captor.getValue().getProviderName());
        assertEquals("My TTS", dto.getProviderName());
    }

    @Test
    void create_voiceCloning_rejectsMissingBaseUrl() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("My TTS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("k")
                .build();
        when(repository.existsByClientIdAndProviderName(CLIENT_ID, "My TTS")).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createProviderCredential(request));
        assertTrue(ex.getMessage().contains("Base URL is required"));
        verify(repository, never()).save(any());
    }

    @Test
    void create_voiceCloning_rejectsUnknownHost() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("My TTS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("k")
                .baseUrl("https://example.com")
                .build();
        when(repository.existsByClientIdAndProviderName(CLIENT_ID, "My TTS")).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.createProviderCredential(request));
        assertTrue(ex.getMessage().contains("ElevenLabs or Fish Audio"));
        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsDuplicateProvider() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("k")
                .build();
        when(repository.existsByClientIdAndProviderName(CLIENT_ID, "ELEVENLABS")).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> service.createProviderCredential(request));
        verify(repository, never()).save(any());
    }

    @Test
    void setDefault_unsetsOtherDefaultsInSameCategory() {
        ProviderCredential target = ProviderCredential.builder()
                .id("pc-1").clientId(CLIENT_ID).providerName("HEYGEN")
                .category(ProviderCategory.VIDEO_RENDERING)
                .apiKey(encryptionService.encrypt("key")).isDefault(false).isActive(true)
                .build();
        ProviderCredential other = ProviderCredential.builder()
                .id("pc-2").clientId(CLIENT_ID).providerName("OTHER_RENDER")
                .category(ProviderCategory.VIDEO_RENDERING)
                .apiKey(encryptionService.encrypt("key2")).isDefault(true).isActive(true)
                .build();
        when(repository.findByIdAndClientId("pc-1", CLIENT_ID)).thenReturn(Optional.of(target));
        when(repository.findByClientIdAndCategory(CLIENT_ID, ProviderCategory.VIDEO_RENDERING))
                .thenReturn(List.of(target, other));
        when(repository.save(any(ProviderCredential.class))).thenAnswer(inv -> inv.getArgument(0));

        ProviderCredentialDto dto = service.setDefault("pc-1");

        assertTrue(dto.getIsDefault());
        assertFalse(other.getIsDefault());
        verify(repository).saveAll(any());
    }

    @Test
    void setActive_falseAlsoUnsetsDefault() {
        ProviderCredential target = ProviderCredential.builder()
                .id("pc-1").clientId(CLIENT_ID).providerName("HEYGEN")
                .category(ProviderCategory.VIDEO_RENDERING)
                .apiKey(encryptionService.encrypt("key")).isDefault(true).isActive(true)
                .build();
        when(repository.findByIdAndClientId("pc-1", CLIENT_ID)).thenReturn(Optional.of(target));
        when(repository.save(any(ProviderCredential.class))).thenAnswer(inv -> inv.getArgument(0));

        ProviderCredentialDto dto = service.setActive("pc-1", false);

        assertFalse(dto.getIsActive());
        assertFalse(dto.getIsDefault());
    }

    @Test
    void update_preservesExistingSecretWhenApiKeyBlank() {
        ProviderCredential existing = ProviderCredential.builder()
                .id("pc-1").clientId(CLIENT_ID).providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey(encryptionService.encrypt("original-key"))
                .baseUrl("https://api.elevenlabs.io")
                .isActive(true).isDefault(false)
                .build();
        when(repository.findByIdAndClientId("pc-1", CLIENT_ID)).thenReturn(Optional.of(existing));
        when(repository.existsByClientIdAndProviderNameAndIdNot(CLIENT_ID, "ELEVENLABS", "pc-1")).thenReturn(false);
        when(repository.save(any(ProviderCredential.class))).thenAnswer(inv -> inv.getArgument(0));

        ProviderCredentialUpdateRequest request = ProviderCredentialUpdateRequest.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("   ")
                .baseUrl("https://api.elevenlabs.io")
                .isActive(true)
                .isDefault(false)
                .build();

        ProviderCredentialDto dto = service.updateProviderCredential("pc-1", request);

        assertEquals("original-key", encryptionService.decrypt(existing.getApiKey()));
        assertEquals("-key", dto.getApiKeyLast4());
    }

    @Test
    void getById_notOwnedThrowsNotFound() {
        when(repository.findByIdAndClientId("pc-x", CLIENT_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProviderCredentialById("pc-x"));
    }

    @Test
    void getProviderCredentials_appliesProviderAndStatusFilters() {
        ProviderCredential cred = ProviderCredential.builder()
                .id("pc-1").clientId(CLIENT_ID).providerName("HEYGEN")
                .category(ProviderCategory.VIDEO_RENDERING)
                .apiKey(encryptionService.encrypt("key")).isActive(true).isDefault(true)
                .build();
        Page<ProviderCredential> page = new PageImpl<>(List.of(cred), PageRequest.of(0, 10), 1);
        when(repository.findByClientIdAndProviderNameIgnoreCaseAndIsActive(
                eq(CLIENT_ID), eq("HEYGEN"), eq(true), any(Pageable.class))).thenReturn(page);

        Page<ProviderCredentialDto> result = service.getProviderCredentials(
                "HEYGEN", true, 0, 10, "createdAt", "desc");

        assertEquals(1, result.getTotalElements());
        assertEquals("HEYGEN", result.getContent().get(0).getProviderName());
    }

    @Test
    void getProviderCredentials_noFiltersUsesClientOnlyQuery() {
        Page<ProviderCredential> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(repository.findByClientId(eq(CLIENT_ID), any(Pageable.class))).thenReturn(page);

        Page<ProviderCredentialDto> result = service.getProviderCredentials(
                null, null, 0, 10, "invalidSort", "weird");

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void delete_removesOwnedCredential() {
        ProviderCredential existing = ProviderCredential.builder()
                .id("pc-1").clientId(CLIENT_ID).providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING).build();
        when(repository.findByIdAndClientId("pc-1", CLIENT_ID)).thenReturn(Optional.of(existing));

        service.deleteProviderCredential("pc-1");

        verify(repository).delete(existing);
    }
}
