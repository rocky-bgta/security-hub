package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import com.aspire.asat.phishing.model.ProviderCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderCredentialMapperTest {

    private final ProviderCredentialMapper mapper = new ProviderCredentialMapper();

    @Test
    void toEntity_mapsFieldsAndKeepsPlaintextSecret() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("  ELEVENLABS ")
                .category(ProviderCategory.VOICE_CLONING)
                .modelName(" eleven_multilingual_v2 ")
                .apiKey("secret-key-1234")
                .apiSecret(" ")
                .baseUrl(" https://api.example.com ")
                .isActive(true)
                .isDefault(true)
                .build();

        ProviderCredential entity = mapper.toEntity(request, "client-1");

        assertEquals("client-1", entity.getClientId());
        assertEquals("ELEVENLABS", entity.getProviderName());
        assertEquals(ProviderCategory.VOICE_CLONING, entity.getCategory());
        assertEquals("eleven_multilingual_v2", entity.getModelName());
        assertEquals("secret-key-1234", entity.getApiKey());
        assertNull(entity.getApiSecret());
        assertEquals("https://api.example.com", entity.getBaseUrl());
        assertTrue(entity.getIsActive());
        assertTrue(entity.getIsDefault());
    }

    @Test
    void toEntity_persistsTypedProviderNameUnchanged() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("  My TTS ")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("fish-key")
                .build();

        ProviderCredential entity = mapper.toEntity(request, "client-1");

        assertEquals("My TTS", entity.getProviderName());
    }

    @Test
    void toEntity_doesNotRewriteSpacedProviderName() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("FISH AUDIO")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("fish-key")
                .build();

        ProviderCredential entity = mapper.toEntity(request, "client-1");

        assertEquals("FISH AUDIO", entity.getProviderName());
    }

    @Test
    void toEntity_blankModelNameStoredAsNull() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .modelName("  ")
                .apiKey("secret-key-1234")
                .build();

        ProviderCredential entity = mapper.toEntity(request, "client-1");

        assertNull(entity.getModelName());
    }

    @Test
    void toDto_masksApiKeyToLast4AndFlagsSecret() {
        ProviderCredential entity = ProviderCredential.builder()
                .id("pc-1")
                .providerName("HEYGEN")
                .category(ProviderCategory.VIDEO_RENDERING)
                .modelName("heygen-avatar-v3")
                .apiSecret("ENC:something")
                .isActive(true)
                .isDefault(false)
                .build();

        ProviderCredentialDto dto = mapper.toDto(entity, "abcd1234WXYZ");

        assertEquals("pc-1", dto.getId());
        assertEquals("WXYZ", dto.getApiKeyLast4());
        assertTrue(dto.getHasApiSecret());
        assertEquals(ProviderCategory.VIDEO_RENDERING, dto.getCategory());
        assertEquals("heygen-avatar-v3", dto.getModelName());
    }

    @Test
    void toDto_withoutSecretFlagsFalse() {
        ProviderCredential entity = ProviderCredential.builder()
                .id("pc-2")
                .providerName("FISH_AUDIO")
                .category(ProviderCategory.VOICE_CLONING)
                .isActive(true)
                .build();

        ProviderCredentialDto dto = mapper.toDto(entity, null);

        assertNull(dto.getApiKeyLast4());
        assertFalse(dto.getHasApiSecret());
    }

    @Test
    void updateFromRequest_onlyOverwritesSecretsWhenProvided() {
        ProviderCredential entity = ProviderCredential.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("ENC:existing")
                .apiSecret("ENC:existing-secret")
                .build();

        ProviderCredentialUpdateRequest request = ProviderCredentialUpdateRequest.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("  ")
                .apiSecret(null)
                .isActive(true)
                .isDefault(false)
                .build();

        mapper.updateFromRequest(entity, request);

        assertEquals("ENC:existing", entity.getApiKey());
        assertEquals("ENC:existing-secret", entity.getApiSecret());
    }

    @Test
    void updateFromRequest_updatesSecretWhenProvided() {
        ProviderCredential entity = ProviderCredential.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("ENC:existing")
                .build();

        ProviderCredentialUpdateRequest request = ProviderCredentialUpdateRequest.builder()
                .providerName("ELEVENLABS")
                .category(ProviderCategory.VOICE_CLONING)
                .modelName(" eleven_turbo_v2 ")
                .apiKey("new-plain-key")
                .isActive(true)
                .isDefault(false)
                .build();

        mapper.updateFromRequest(entity, request);

        assertEquals("new-plain-key", entity.getApiKey());
        assertEquals("eleven_turbo_v2", entity.getModelName());
    }
}
