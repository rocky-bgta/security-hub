package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.ProviderCredential;
import com.aspire.asat.phishing.repository.ProviderCredentialRepository;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderCredentialResolverImplTest {

    @Mock
    private ProviderCredentialRepository providerCredentialRepository;

    private CredentialEncryptionService encryptionService;
    private ProviderCredentialResolverImpl resolver;

    @BeforeEach
    void setUp() {
        encryptionService = new CredentialEncryptionService(Base64.getEncoder().encodeToString(new byte[32]));
        resolver = new ProviderCredentialResolverImpl(
                providerCredentialRepository,
                encryptionService,
                "yaml-eleven-key", "https://yaml.elevenlabs",
                "yaml-fish-key", "https://yaml.fish",
                "yaml-heygen-key", "https://yaml.heygen");
    }

    @Test
    void resolve_returnsDecryptedPerClientCredentialWhenActive() {
        ProviderCredential cred = ProviderCredential.builder()
                .clientId("client-1")
                .providerName("ELEVENLABS")
                .modelName("eleven_multilingual_v2")
                .apiKey(encryptionService.encrypt("client-key"))
                .apiSecret(encryptionService.encrypt("client-secret"))
                .baseUrl("https://client.elevenlabs")
                .isActive(true)
                .build();
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "ELEVENLABS"))
                .thenReturn(List.of(cred));

        ResolvedProviderCredentials result = resolver.resolve("client-1", "ELEVENLABS");

        assertEquals("client-key", result.getApiKey());
        assertEquals("client-secret", result.getApiSecret());
        assertEquals("https://client.elevenlabs", result.getBaseUrl());
        assertEquals("eleven_multilingual_v2", result.getModelName());
        assertTrue(result.hasModelName());
    }

    @Test
    void resolve_matchesStoredProviderNameCaseInsensitively() {
        ProviderCredential cred = ProviderCredential.builder()
                .clientId("client-1")
                .providerName("ElevenLabs")
                .apiKey(encryptionService.encrypt("client-key"))
                .isActive(true)
                .build();
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "ELEVENLABS"))
                .thenReturn(List.of(cred));

        ResolvedProviderCredentials result = resolver.resolve("client-1", "ELEVENLABS");

        assertEquals("client-key", result.getApiKey());
    }

    @Test
    void resolve_prefersActiveCredentialWhenMultipleMatch() {
        ProviderCredential inactive = ProviderCredential.builder()
                .clientId("client-1").providerName("elevenlabs")
                .apiKey(encryptionService.encrypt("inactive-key")).isActive(false)
                .build();
        ProviderCredential active = ProviderCredential.builder()
                .clientId("client-1").providerName("ELEVENLABS")
                .apiKey(encryptionService.encrypt("active-key")).isActive(true)
                .build();
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "ELEVENLABS"))
                .thenReturn(List.of(inactive, active));

        ResolvedProviderCredentials result = resolver.resolve("client-1", "ELEVENLABS");

        assertEquals("active-key", result.getApiKey());
    }

    @Test
    void resolve_fallsBackToYamlWhenNoPerClientCredential() {
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "HEYGEN"))
                .thenReturn(List.of());

        ResolvedProviderCredentials result = resolver.resolve("client-1", "HEYGEN");

        assertEquals("yaml-heygen-key", result.getApiKey());
        assertEquals("https://yaml.heygen", result.getBaseUrl());
        assertNull(result.getModelName());
        assertFalse(result.hasModelName());
    }

    @Test
    void resolve_fallsBackToYamlWhenPerClientCredentialInactive() {
        ProviderCredential cred = ProviderCredential.builder()
                .clientId("client-1")
                .providerName("FISH_AUDIO")
                .apiKey(encryptionService.encrypt("ignored"))
                .isActive(false)
                .build();
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "FISH_AUDIO"))
                .thenReturn(List.of(cred));

        ResolvedProviderCredentials result = resolver.resolve("client-1", "FISH_AUDIO");

        assertEquals("yaml-fish-key", result.getApiKey());
        assertTrue(result.hasApiKey());
    }

    @Test
    void resolve_canonicalizesSpacedFishAudioNameForYamlFallback() {
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "FISH_AUDIO"))
                .thenReturn(List.of());

        ResolvedProviderCredentials result = resolver.resolve("client-1", "FISH AUDIO");

        assertEquals("yaml-fish-key", result.getApiKey());
        assertEquals("FISH_AUDIO", result.getProviderName());
    }

    @Test
    void resolve_unknownProviderWithoutCredentialHasNoApiKey() {
        when(providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase("client-1", "SOMETHING_NEW"))
                .thenReturn(List.of());

        ResolvedProviderCredentials result = resolver.resolve("client-1", "SOMETHING_NEW");

        assertNull(result.getApiKey());
        assertFalse(result.hasApiKey());
    }

    @Test
    void resolve_withoutClientIdUsesYamlFallback() {
        ResolvedProviderCredentials result = resolver.resolve(null, "ELEVENLABS");

        assertEquals("yaml-eleven-key", result.getApiKey());
    }

    @Test
    void resolveById_returnsDecryptedActiveCredential() {
        ProviderCredential cred = ProviderCredential.builder()
                .id("pc-1")
                .clientId("client-1")
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .modelName("eleven_multilingual_v2")
                .apiKey(encryptionService.encrypt("client-key"))
                .apiSecret(encryptionService.encrypt("client-secret"))
                .baseUrl("https://client.elevenlabs")
                .isActive(true)
                .build();
        when(providerCredentialRepository.findByIdAndClientId("pc-1", "client-1"))
                .thenReturn(Optional.of(cred));

        ResolvedProviderCredentials result = resolver.resolveById("client-1", "pc-1");

        assertEquals("ELEVENLABS", result.getProviderName());
        assertEquals("client-key", result.getApiKey());
        assertEquals("client-secret", result.getApiSecret());
        assertEquals("https://client.elevenlabs", result.getBaseUrl());
        assertEquals("eleven_multilingual_v2", result.getModelName());
        assertEquals(ProviderCategory.VOICE_CLONING, result.getCategory());
    }

    @Test
    void resolveById_notFound_throwsResourceNotFound() {
        when(providerCredentialRepository.findByIdAndClientId("missing", "client-1"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resolver.resolveById("client-1", "missing"));
    }

    @Test
    void resolveById_inactive_throwsServiceException() {
        ProviderCredential cred = ProviderCredential.builder()
                .id("pc-inactive")
                .clientId("client-1")
                .providerName("HEYGEN")
                .category(ProviderCategory.VIDEO_RENDERING)
                .apiKey(encryptionService.encrypt("key"))
                .isActive(false)
                .build();
        when(providerCredentialRepository.findByIdAndClientId("pc-inactive", "client-1"))
                .thenReturn(Optional.of(cred));

        assertThrows(ServiceException.class,
                () -> resolver.resolveById("client-1", "pc-inactive"));
    }

    @Test
    void resolveById_blankIds_throwsServiceException() {
        assertThrows(ServiceException.class, () -> resolver.resolveById("client-1", "  "));
        assertThrows(ServiceException.class, () -> resolver.resolveById(null, "pc-1"));
    }
}
