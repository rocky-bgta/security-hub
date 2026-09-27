package com.aspire.asat.phishing.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishAudioClientBaseUrlTest {

    @Test
    void normalizeFishAudioBaseUrl_rewritesMarketingHostToApiHost() {
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl("https://fish.audio"));
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl("https://fish.audio/"));
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl("http://www.fish.audio"));
        assertEquals("https://api.fish.audio/model",
                FishAudioClient.normalizeFishAudioBaseUrl("https://fish.audio/model"));
    }

    @Test
    void normalizeFishAudioBaseUrl_keepsApiHostAndBlankDefault() {
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl("https://api.fish.audio"));
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl("https://api.fish.audio/"));
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl("  "));
        assertEquals("https://api.fish.audio", FishAudioClient.normalizeFishAudioBaseUrl(null));
    }
}
