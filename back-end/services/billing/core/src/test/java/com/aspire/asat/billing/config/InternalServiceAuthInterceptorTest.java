package com.aspire.asat.billing.config;

import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.common.constants.InternalServiceAuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class InternalServiceAuthInterceptorTest {

    private static final String API_KEY = "test-internal-key";

    @Mock
    private HttpServletResponse response;

    private InternalServiceAuthInterceptor interceptor;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        interceptor = new InternalServiceAuthInterceptor();
        setField(interceptor, "apiKey", API_KEY);
        setField(interceptor, "authEnabled", true);
    }

    @Test
    void preHandle_allowsCreateWhenInternalKeyMatches() {
        HttpServletRequest request = protectedCreateRequest(API_KEY);

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_rejectsCreateWhenInternalKeyMissing() {
        HttpServletRequest request = protectedCreateRequest(null);

        BillingServiceException ex = assertThrows(
                BillingServiceException.class,
                () -> interceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void preHandle_rejectsCreateWhenInternalKeyWrong() {
        HttpServletRequest request = protectedCreateRequest("wrong-key");

        BillingServiceException ex = assertThrows(
                BillingServiceException.class,
                () -> interceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void preHandle_allowsUnprotectedInvoiceListPath() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn("/api/v1/invoice/list");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    private static HttpServletRequest protectedCreateRequest(String apiKey) {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/api/v1/invoice/create");
        when(request.getHeader(InternalServiceAuthConstants.HEADER_NAME)).thenReturn(apiKey);
        return request;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
