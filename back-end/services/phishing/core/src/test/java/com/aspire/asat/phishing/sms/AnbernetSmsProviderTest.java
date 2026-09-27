package com.aspire.asat.phishing.sms;

import com.aspire.asat.phishing.dto.enums.SmsProviderType;
import com.aspire.asat.phishing.sms.impl.AnbernetSmsProvider;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnbernetSmsProviderTest {

    private HttpServer httpServer;
    private String baseUrl;
    private final AtomicReference<String> capturedPath = new AtomicReference<>();
    private final AtomicReference<String> capturedBody = new AtomicReference<>();
    private final AtomicReference<String> capturedAuth = new AtomicReference<>();
    private final AtomicReference<String> cannedResponse = new AtomicReference<>(
            "[{\"status\":\"success\",\"success_count\":1},200]");
    private final AtomicReference<Integer> cannedStatus = new AtomicReference<>(200);

    @BeforeEach
    void setUp() throws Exception {
        cannedStatus.set(200);
        cannedResponse.set("[{\"status\":\"success\",\"success_count\":1},200]");
        capturedPath.set(null);
        capturedBody.set(null);
        capturedAuth.set(null);
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/", exchange -> {
            capturedPath.set(exchange.getRequestURI().getPath());
            capturedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
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
    void send_UsesNameAsAccountAndPostsToBaseUrl() {
        AnbernetSmsProvider provider = new AnbernetSmsProvider(
                "Aspire-Tech",
                "test-api-key",
                "8809639210610",
                baseUrl,
                Map.of());

        SmsSendResult result = provider.send("+8801773126589", "Hi from Aspire");

        assertTrue(result.success());
        assertEquals("/api/v1/sendsms", capturedPath.get());
        assertNull(capturedAuth.get());
        String body = capturedBody.get();
        assertTrue(body.contains("\"account\":\"Aspire-Tech\""));
        assertTrue(body.contains("\"api_key\":\"test-api-key\""));
        assertTrue(body.contains("\"senderid\":\"8809639210610\""));
        assertTrue(body.contains("\"receivers\":[\"+8801773126589\"]"));
        assertTrue(body.contains("\"msgdata\":\"Hi from Aspire\""));
        assertFalse(body.contains("api_secret"));
    }

    @Test
    void send_MetadataAccountOverridesName() {
        AnbernetSmsProvider provider = new AnbernetSmsProvider(
                "BD provider",
                "test-api-key",
                "8809639210610",
                baseUrl,
                Map.of("account", "Aspire-Tech"));

        SmsSendResult result = provider.send("+8801773126589", "test");

        assertTrue(result.success());
        assertTrue(capturedBody.get().contains("\"account\":\"Aspire-Tech\""));
    }

    @Test
    void send_BangladeshTrunkZero_NormalizesReceiver() {
        AnbernetSmsProvider provider = new AnbernetSmsProvider(
                "Aspire-Tech",
                "test-api-key",
                "8809639210610",
                baseUrl,
                Map.of());

        SmsSendResult result = provider.send("+88001773126589", "test");

        assertTrue(result.success());
        assertTrue(capturedBody.get().contains("\"receivers\":[\"+8801773126589\"]"));
    }

    @Test
    void send_MissingName_ReturnsConfigErrorWithoutCallingApi() {
        AnbernetSmsProvider provider = new AnbernetSmsProvider(
                null,
                "test-api-key",
                "8809639210610",
                baseUrl,
                Map.of());

        SmsSendResult result = provider.send("+8801773126589", "test");

        assertFalse(result.success());
        assertEquals("ANBERNET_CONFIG_ERROR", result.errorCode());
        assertNull(capturedPath.get());
    }

    @Test
    void send_UnprocessableEntity_IncludesApiResponseBody() {
        cannedStatus.set(422);
        cannedResponse.set("{\"error\":\"invalid payload\"}");
        AnbernetSmsProvider provider = new AnbernetSmsProvider(
                "Aspire-Tech",
                "test-api-key",
                "8809639210610",
                baseUrl,
                Map.of());

        SmsSendResult result = provider.send("+8801773126589", "test");

        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("422"));
        assertTrue(result.errorMessage().contains("invalid payload"));
    }

    @Test
    void getProviderType_ReturnsGenericRest() {
        AnbernetSmsProvider provider = new AnbernetSmsProvider(
                "Aspire-Tech", "key", "sender", baseUrl, Map.of());

        assertEquals(SmsProviderType.GENERIC_REST, provider.getProviderType());
    }
}
