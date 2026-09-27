package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsMicroContentClientDeleteTest {

    private static final String CONTEXT_HEADER = "base64-current-context";

    @Mock
    private UserCurrentContextService userCurrentContextService;

    private HttpServer server;
    private CmsMicroContentClient client;
    private final AtomicReference<String> requestMethod = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> currentContextHeader = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            requestPath.set(exchange.getRequestURI().getPath());
            currentContextHeader.set(exchange.getRequestHeaders()
                    .getFirst(UserCurrentContextService.HEADER_CURRENT_USER_CONTEXT));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        when(userCurrentContextService.getCurrentUserContextHeaderValue()).thenReturn(CONTEXT_HEADER);
        client = new CmsMicroContentClient(WebClient.builder().build(), userCurrentContextService);
        setField(client, "cmsServiceUrl", "http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void deleteTopic_ForwardsCurrentContextHeader() {
        client.deleteTopic("topic-123");

        assertEquals("DELETE", requestMethod.get());
        assertEquals("/topics/topic-123", requestPath.get());
        assertEquals(CONTEXT_HEADER, currentContextHeader.get());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
