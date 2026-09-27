package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElevenLabsClientApiKeyTest {

    private final ElevenLabsClient client = new ElevenLabsClient(WebClient.builder().build());

    @Test
    void addVoice_missingProviderConfigKey_throws() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> client.addVoice(new File("unused.wav"), "name",
                        ResolvedProviderCredentials.builder().build()));

        assertEquals("ElevenLabs API key is not configured", ex.getMessage());
    }
}
