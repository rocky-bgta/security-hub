package com.aspire.asat.phishing.sms;

import com.aspire.asat.phishing.sms.impl.GenericRestSmsProvider;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericRestSmsProviderTest {

    private HttpServer httpServer;
    private String baseUrl;
    private final AtomicReference<String> capturedPath = new AtomicReference<>();
    private final AtomicReference<String> capturedBody = new AtomicReference<>();
    private final AtomicReference<String> cannedResponse = new AtomicReference<>("{\"status\":\"ok\"}");
    private final AtomicReference<Integer> cannedStatus = new AtomicReference<>(200);

    @BeforeEach
    void setUp() throws Exception {
        cannedStatus.set(200);
        cannedResponse.set("{\"status\":\"ok\"}");
        capturedPath.set(null);
        capturedBody.set(null);
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/", exchange -> {
            capturedPath.set(exchange.getRequestURI().getPath());
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            capturedBody.set(new String(requestBytes, StandardCharsets.UTF_8));
            byte[] response = cannedResponse.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(cannedStatus.get(), response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        httpServer.start();
        baseUrl = "http://localhost:" + httpServer.getAddress().getPort() + "/api/v1/sendsms";
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void send_DefaultMetadata_PostsToBaseUrlWithoutSendPath() {
        GenericRestSmsProvider provider = new GenericRestSmsProvider(
                "api-key", "api-secret", "sender-1", baseUrl, Map.of());

        SmsSendResult result = provider.send("+15551234567", "hello");

        assertTrue(result.success());
        assertEquals("/api/v1/sendsms", capturedPath.get());
        String body = capturedBody.get();
        assertTrue(body.contains("\"to\":\"+15551234567\""));
        assertTrue(body.contains("\"message\":\"hello\""));
        assertTrue(body.contains("\"sender\":\"sender-1\""));
        assertTrue(body.contains("\"api_key\":\"api-key\""));
        assertTrue(body.contains("\"api_secret\":\"api-secret\""));
    }

    @Test
    void send_ExplicitSendPath_AppendsToBaseUrl() {
        GenericRestSmsProvider provider = new GenericRestSmsProvider(
                "api-key", "api-secret", "sender-1", baseUrl,
                Map.of("sendPath", "/send"));

        SmsSendResult result = provider.send("+15551234567", "hello");

        assertTrue(result.success());
        assertEquals("/api/v1/sendsms/send", capturedPath.get());
    }

    @Test
    void send_UnprocessableEntity_IncludesApiResponseBody() {
        cannedStatus.set(422);
        cannedResponse.set("{\"error\":\"account is required\"}");
        GenericRestSmsProvider provider = new GenericRestSmsProvider(
                "test-api-key",
                "unused-secret",
                "sender-1",
                baseUrl,
                Map.of());

        SmsSendResult result = provider.send("+15551234567", "hello");

        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("422"));
        assertTrue(result.errorMessage().contains("account is required"));
    }
}
