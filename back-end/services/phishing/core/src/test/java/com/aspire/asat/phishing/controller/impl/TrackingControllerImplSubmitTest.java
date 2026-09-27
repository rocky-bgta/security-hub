package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.service.TrackingService;
import com.aspire.asat.phishing.service.support.SubmissionAwarenessPageRenderer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingControllerImplSubmitTest {

    @Mock
    private TrackingService trackingService;
    @Mock
    private SubmissionAwarenessPageRenderer submissionAwarenessPageRenderer;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TrackingControllerImpl controller;

    @Test
    void trackSubmit_returnsHtmlAwarenessPage() {
        when(trackingService.recordSubmission(anyString(), any(), any(), any()))
                .thenReturn("https://example.com/safe");
        when(submissionAwarenessPageRenderer.render("https://example.com/safe"))
                .thenReturn("<html><body>Alert</body></html>");

        ResponseEntity<String> response = controller.trackSubmit(
                "trk-1",
                Map.of("username", "user@example.com"),
                request
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Alert"));
        assertFalse(response.getHeaders().containsKey("Location"));
        verify(submissionAwarenessPageRenderer).render("https://example.com/safe");
    }

    @Test
    void trackSubmit_rendersAwarenessPageWhenRedirectUnresolved() {
        when(trackingService.recordSubmission(anyString(), any(), any(), any()))
                .thenReturn(null);
        when(submissionAwarenessPageRenderer.render("/"))
                .thenReturn("<html><body>Fallback</body></html>");

        ResponseEntity<String> response = controller.trackSubmit("trk-1", Map.of(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submissionAwarenessPageRenderer).render("/");
    }
}
