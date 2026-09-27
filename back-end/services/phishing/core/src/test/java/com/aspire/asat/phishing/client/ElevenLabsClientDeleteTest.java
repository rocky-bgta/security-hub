package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElevenLabsClientDeleteTest {

    private HttpServer server;
    private ElevenLabsClient client;
    private ResolvedProviderCredentials credentials;
    private final AtomicInteger responseStatus = new AtomicInteger(204);
    private final AtomicReference<String> responseBody = new AtomicReference<>("");
    private final AtomicReference<String> requestMethod = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> apiKey = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            requestPath.set(exchange.getRequestURI().getPath());
            apiKey.set(exchange.getRequestHeaders().getFirst("xi-api-key"));
            byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus.get(), body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();

        client = new ElevenLabsClient(WebClient.builder().build());
        setField(client, "timeoutSeconds", 5L);
        credentials = ResolvedProviderCredentials.builder()
                .apiKey("test-api-key")
                .baseUrl("http://localhost:" + server.getAddress().getPort())
                .build();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void deleteVoice_success_sendsExpectedDeleteRequest() {
        client.deleteVoice("voice-123", credentials);

        assertEquals("DELETE", requestMethod.get());
        assertEquals("/v1/voices/voice-123", requestPath.get());
        assertEquals("test-api-key", apiKey.get());
    }

    @Test
    void deleteVoice_providerReturnsNotFound_treatsAsAlreadyDeleted() {
        responseStatus.set(404);

        assertDoesNotThrow(() -> client.deleteVoice("voice-missing", credentials));
        assertEquals("DELETE", requestMethod.get());
    }

    @Test
    void deleteVoice_providerReturnsVoiceNotFoundAsBadRequest_treatsAsAlreadyDeleted() {
        responseStatus.set(400);
        responseBody.set("{\"detail\":{\"type\":\"not_found\",\"code\":\"voice_not_found\","
                + "\"message\":\"A voice with ID 'voice-missing' was not found.\","
                + "\"status\":\"voice_does_not_exist\"}}");

        assertDoesNotThrow(() -> client.deleteVoice("voice-missing", credentials));
        assertEquals("DELETE", requestMethod.get());
    }

    @Test
    void deleteVoice_providerFailure_throwsServiceException() {
        responseStatus.set(500);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> client.deleteVoice("voice-failed", credentials));

        assertEquals("ElevenLabs voice delete failed: 500 Internal Server Error body=",
                exception.getMessage());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
