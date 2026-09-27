package com.aspire.asat.auth.exception.handler;

import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.apiResponses.error.ErrorResponseDTO;
import com.aspire.asat.auth.exception.OtpCooldownException;
import com.aspire.asat.auth.exception.OtpInvalidException;
import com.aspire.asat.auth.exception.OtpLockedException;
import com.aspire.asat.auth.service.LocaleMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerOtpTest {

    @Mock
    private LocaleMessageService localeMessageService;
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/mfa/verify-otp");
        when(localeMessageService.getLocalMessage(anyString())).thenAnswer(inv -> translated(inv.getArgument(0)));
    }

    @Test
    void handleServiceException_IncludesRetryAfterAndErrorCodeForLock() {
        OtpLockedException ex = new OtpLockedException(OtpMessages.LOCKED, 900);

        ResponseEntity<ErrorResponseDTO> response = handler.handleServiceException(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("900", response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertEquals("OTP_LOCKED", response.getBody().getErrorCode());
        assertEquals(900, response.getBody().getRetryAfterSeconds());
        assertEquals("Too many incorrect attempts. Verification has been temporarily locked.",
                response.getBody().getMessage());
    }

    @Test
    void handleServiceException_IncludesRetryAfterForCooldown() {
        OtpCooldownException ex = new OtpCooldownException(42);

        ResponseEntity<ErrorResponseDTO> response = handler.handleServiceException(ex, request);

        assertEquals("42", response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertEquals("OTP_COOLDOWN", response.getBody().getErrorCode());
        assertEquals("Too many verification code requests. Please try again later.",
                response.getBody().getMessage());
    }

    @Test
    void handleServiceException_InvalidOtp_UsesSpecMessage() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleServiceException(new OtpInvalidException(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("OTP_INVALID", response.getBody().getErrorCode());
        assertEquals("Invalid verification code. Please try again.", response.getBody().getMessage());
    }

    private static String translated(String key) {
        return switch (key) {
            case OtpMessages.INVALID -> "Invalid verification code. Please try again.";
            case OtpMessages.EXPIRED -> "This verification code has expired. Please request a new code.";
            case OtpMessages.LOCKED -> "Too many incorrect attempts. Verification has been temporarily locked.";
            case OtpMessages.RATE_LIMITED -> "Too many verification code requests. Please try again later.";
            default -> key;
        };
    }
}
