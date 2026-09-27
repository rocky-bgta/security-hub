package com.aspire.asat.phishing.service.impl.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BrowserCaptureClientTest {

    @Test
    void shouldReturnDisabledWhenFeatureFlagOff() {
        BrowserCaptureClient client = new BrowserCaptureClient(new ObjectMapper());
        BrowserCaptureClient.BrowserCaptureResult result = client.capture("https://example.com", true, true);

        Assertions.assertFalse(result.enabled());
        Assertions.assertFalse(result.success());
        Assertions.assertEquals("disabled", result.errorReason());
    }
}

