package com.aspire.asat.phishing.service.impl.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CookieJarTest {

    @Test
    void shouldCaptureAndRenderCookieHeader() {
        CookieJar jar = new CookieJar();
        jar.capture(List.of(
                "cf_clearance=abc123; Path=/; HttpOnly",
                "session_id=xyz789; Secure; SameSite=Lax"
        ));

        String header = jar.asHeader();
        Assertions.assertTrue(header.contains("cf_clearance=abc123"));
        Assertions.assertTrue(header.contains("session_id=xyz789"));
        Assertions.assertEquals(2, jar.size());
        Assertions.assertFalse(jar.isEmpty());
    }

    @Test
    void shouldOverwriteExistingCookieValueByName() {
        CookieJar jar = new CookieJar();
        jar.capture(List.of("token=old; Path=/"));
        jar.capture(List.of("token=new; Path=/"));

        Assertions.assertEquals(1, jar.size());
        Assertions.assertTrue(jar.asHeader().contains("token=new"));
    }
}

