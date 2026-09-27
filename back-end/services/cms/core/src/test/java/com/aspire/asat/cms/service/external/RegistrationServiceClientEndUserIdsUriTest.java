package com.aspire.asat.cms.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RegistrationServiceClientEndUserIdsUriTest {

    private RegistrationServiceClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new RegistrationServiceClient(mock(WebClient.class), new ObjectMapper());
        Field field = RegistrationServiceClient.class.getDeclaredField("registrationUrl");
        field.setAccessible(true);
        field.set(client, "http://registration:8080/api/v1");
    }

    @Test
    void buildEndUserIdsUri_departmentWithSlash_doesNotPercentEncodeSlash() {
        URI uri = client.buildEndUserIdsUri(
                List.of("client-1"), null, "Audit/Internal Controls");

        String raw = uri.toString();
        assertTrue(raw.contains("departments="));
        assertTrue(raw.contains("Audit"));
        assertTrue(raw.contains("Internal"));
        assertFalse(raw.contains("%2F"), raw);
        assertFalse(raw.contains("%252F"), raw);
    }
}
