package com.aspire.asat.auth.exception.handler;

import com.aspire.asat.auth.dto.apiResponses.error.ErrorResponseDTO;
import com.aspire.asat.auth.exception.MfaException;
import com.aspire.asat.auth.exception.ServiceException;
import com.aspire.asat.auth.service.LocaleMessageService;
import com.aspire.asat.common.exception.AspireException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final LocaleMessageService localeMessageService;

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.warn("Auth Service Exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        String errorCode = null;
        Integer retryAfterSeconds = null;
        if (ex instanceof MfaException mfaException) {
            errorCode = mfaException.getErrorCode();
            retryAfterSeconds = mfaException.getRetryAfterSeconds();
        }
        return buildErrorResponse("Auth Service Error", localeMessageService.getLocalMessage(ex.getMessage()),
                ex.getStatus(), request, errorCode, retryAfterSeconds);
    }

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ErrorResponseDTO> handleAspireException(AspireException ex, HttpServletRequest request) {
        log.warn("AspireException at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Aspire Error", localeMessageService.getLocalMessage(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation error at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", localeMessageService.getLocalMessage(message), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst().orElse("Constraint violation");
        log.warn("Constraint violation at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", localeMessageService.getLocalMessage(message), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        // Log at debug level since this is common (e.g., GET on POST-only endpoints, Swagger UI exploration)
        log.debug("Method not supported at [{}]: {} method is not supported. Supported methods: {}", 
                request.getRequestURI(), ex.getMethod(), ex.getSupportedHttpMethods());
        // Don't try to localize technical error messages - use the exception message directly
        String message = String.format("Method %s is not supported for this endpoint. Supported methods: %s", 
                ex.getMethod(), ex.getSupportedHttpMethods());
        // Return response directly without localization to avoid message key lookup errors
        return buildErrorResponse("Method Not Allowed", message, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Unexpected Error", localeMessageService.getLocalMessage(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String errorType, String message, HttpStatus status, HttpServletRequest request) {
        return buildErrorResponse(errorType, message, status, request, null, null);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String errorType, String message, HttpStatus status,
                                                                HttpServletRequest request, String errorCode,
                                                                Integer retryAfterSeconds) {
        HttpHeaders headers = new HttpHeaders();
        if (retryAfterSeconds != null && retryAfterSeconds > 0) {
            headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        }
        ErrorResponseDTO body = new ErrorResponseDTO(
                errorType, message, status.value(), request.getRequestURI(), LocalDateTime.now(),
                errorCode, retryAfterSeconds);
        return new ResponseEntity<>(body, headers, status);
    }
}
