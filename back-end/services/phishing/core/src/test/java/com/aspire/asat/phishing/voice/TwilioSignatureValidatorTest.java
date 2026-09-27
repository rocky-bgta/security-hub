package com.aspire.asat.phishing.voice;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwilioSignatureValidatorTest {

    private static final String AUTH_TOKEN = "test-auth-token";
    private static final String URL = "https://dev.example.com/gateway/phishing/v/calls/trk-1/gather";

    private final TwilioSignatureValidator validator = new TwilioSignatureValidator();

    @Test
    void validSignature_passes() throws Exception {
        Map<String, String> params = new TreeMap<>();
        params.put("Digits", "1234");
        params.put("CallSid", "CA123");
        String signature = computeSignature(AUTH_TOKEN, URL, params);

        assertTrue(validator.isValid(AUTH_TOKEN, URL, params, signature));
    }

    @Test
    void tamperedSignature_fails() {
        Map<String, String> params = Map.of("Digits", "1234");

        assertFalse(validator.isValid(AUTH_TOKEN, URL, params, "invalid-signature"));
    }

    @Test
    void missingAuthToken_fails() {
        assertFalse(validator.isValid(" ", URL, Map.of(), "sig"));
    }

    @Test
    void missingSignature_fails() {
        assertFalse(validator.isValid(AUTH_TOKEN, URL, Map.of(), " "));
    }

    private static String computeSignature(String authToken, String url, Map<String, String> params) throws Exception {
        StringBuilder builder = new StringBuilder(url);
        // Twilio concatenates the URL with each POST param, sorted by key.
        new TreeMap<>(params).forEach((k, v) -> builder.append(k).append(v));
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] hmac = mac.doFinal(builder.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }
}
