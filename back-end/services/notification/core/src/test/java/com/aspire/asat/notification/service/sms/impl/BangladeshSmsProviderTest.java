package com.aspire.asat.notification.service.sms.impl;

import com.aspire.asat.notification.util.PhoneNumberParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BangladeshSmsProviderTest {

    private BangladeshSmsProvider provider;
    private HttpServer httpServer;
    private final AtomicReference<String> capturedBody = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        provider = new BangladeshSmsProvider(new PhoneNumberParser(), WebClient.builder().build());
        setField(provider, "enabled", true);
        setField(provider, "account", "Aspire-Tech");
        setField(provider, "apiKey", "test-api-key");
        setField(provider, "senderId", "8809639174000");
        setField(provider, "countryCodes", "+880");

        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/api/v1/sendsms", exchange -> {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            capturedBody.set(new String(requestBytes, StandardCharsets.UTF_8));
            byte[] response = ("[{\"status\":\"success\",\"success_count\":1},200]")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        httpServer.start();

        String apiUrl = "http://localhost:" + httpServer.getAddress().getPort() + "/api/v1/sendsms";
        setField(provider, "apiUrl", apiUrl);
        provider.init();
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void supportsCountry_BangladeshCode_ReturnsTrue() {
        assertTrue(provider.supportsCountry("+880"));
        assertFalse(provider.supportsCountry("+1"));
    }

    @Test
    void isSuccessfulResponse_AnbernetSuccessPayload_ReturnsTrue() {
        String response = "[{\"status\":\"success\",\"account\":\"Aspire-Tech\",\"success_count\":1,\"failed_count\":0},200]";
        assertTrue(provider.isSuccessfulResponse(response));
        assertFalse(provider.isSuccessfulResponse("{\"status\":\"failed\"}"));
        assertFalse(provider.isSuccessfulResponse(null));
    }

    @Test
    void sendSms_ConfiguredProvider_PostsAnbernetPayloadAndReturnsTrue() {
        boolean result = provider.sendSms("+8801773126589", "checking the sms api from unit test");

        assertTrue(result);
        String body = capturedBody.get();
        assertTrue(body.contains("\"account\":\"Aspire-Tech\""));
        assertTrue(body.contains("\"api_key\":\"test-api-key\""));
        assertTrue(body.contains("\"senderid\":\"8809639174000\""));
        assertTrue(body.contains("\"receivers\":[\"+8801773126589\"]"));
        assertTrue(body.contains("\"msgdata\":\"checking the sms api from unit test\""));
    }

    @Test
    void sendSms_NotConfigured_ReturnsFalse() throws Exception {
        setField(provider, "apiKey", "");
        provider.init();

        assertFalse(provider.sendSms("+8801773126589", "test"));
        assertEquals(null, capturedBody.get());
    }

    @Test
    void sendSms_Disabled_ReturnsFalse() throws Exception {
        setField(provider, "enabled", false);

        assertFalse(provider.sendSms("+8801773126589", "test"));
        assertEquals(null, capturedBody.get());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
