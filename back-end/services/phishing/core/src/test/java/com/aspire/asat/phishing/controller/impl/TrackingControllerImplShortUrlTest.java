package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.service.TrackingService;
import com.aspire.asat.phishing.service.UrlShortenerService;
import com.aspire.asat.phishing.service.support.RedirectUrlSanitizer;
import com.aspire.asat.phishing.service.support.SubmissionAwarenessPageRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingControllerImplShortUrlTest {

    private static final String ORIGINAL_URL =
            "https://online-banking.tech/dev/gateway/phishing/t/phish/225cb258f8404dde9541e51c3af83662";

    @Mock
    private TrackingService trackingService;
    @Mock
    private SubmissionAwarenessPageRenderer submissionAwarenessPageRenderer;
    @Mock
    private UrlShortenerService urlShortenerService;
    @Mock
    private RedirectUrlSanitizer redirectUrlSanitizer;

    @InjectMocks
    private TrackingControllerImpl controller;

    @Test
    void redirectShortUrl_found_returns302ToOriginalLandingUrl() {
        when(urlShortenerService.resolve("01k7Qm2Nxp")).thenReturn(Optional.of(ORIGINAL_URL));
        when(redirectUrlSanitizer.sanitizeOptional(ORIGINAL_URL)).thenReturn(ORIGINAL_URL);

        ResponseEntity<String> response = controller.redirectShortUrl("01k7Qm2Nxp");

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(ORIGINAL_URL, response.getHeaders().getFirst(HttpHeaders.LOCATION));
        verify(trackingService, never()).recordClick(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(trackingService, never()).serveLandingPage(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void redirectShortUrl_unknown_returns404Html() {
        when(urlShortenerService.resolve("missing")).thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.redirectShortUrl("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getFirst(HttpHeaders.LOCATION));
        assertTrue(response.getBody() != null && response.getBody().contains("Page Not Found"));
        verify(trackingService, never()).recordClick(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void redirectShortUrl_unsafeOriginal_returns404() {
        when(urlShortenerService.resolve("01k7Qm2Nxp")).thenReturn(Optional.of("javascript:alert(1)"));
        when(redirectUrlSanitizer.sanitizeOptional("javascript:alert(1)")).thenReturn(null);

        ResponseEntity<String> response = controller.redirectShortUrl("01k7Qm2Nxp");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
