package com.aspire.asat.phishing.util;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProviderNameNormalizerTest {

    @Test
    void canonicalize_NormalizesSpacesHyphensAndCase() {
        assertEquals("FISH_AUDIO", ProviderNameNormalizer.canonicalize("FISH AUDIO"));
        assertEquals("FISH_AUDIO", ProviderNameNormalizer.canonicalize(" fish-audio "));
        assertEquals("ELEVENLABS", ProviderNameNormalizer.canonicalize("ElevenLabs"));
        assertNull(ProviderNameNormalizer.canonicalize("  "));
        assertNull(ProviderNameNormalizer.canonicalize(null));
    }

    @Test
    void parseEnum_MatchesSpacedAndCompactNames() {
        assertEquals(VoiceCloneProvider.FISH_AUDIO,
                ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, "FISH AUDIO"));
        assertEquals(VoiceCloneProvider.FISH_AUDIO,
                ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, "fish_audio"));
        assertEquals(VoiceCloneProvider.ELEVENLABS,
                ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, "ELEVEN LABS"));
        assertEquals(VoiceCloneProvider.ELEVENLABS,
                ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, "ElevenLabs"));
        assertNull(ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, "NOT_A_PROVIDER"));
        assertNull(ProviderNameNormalizer.parseEnum(VoiceCloneProvider.class, "  "));
    }
}
