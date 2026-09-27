package com.aspire.asat.notification.exception.handler;

import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.common.exception.ServiceException;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Global exception handler for Notification Service
 * Handles all exceptions thrown by controllers and services
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.aspire.asat.notification")
public class GlobalExceptionHandler {

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAspireException(AspireException ex) {
        log.warn("AspireException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleServiceException(ServiceException ex) {
        log.warn("ServiceException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiResponseDto<>(ex.getMessage(), ex.getStatus().value(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("ResourceNotFoundException at [{}]: {}", getRequestPath(request), ex.getMessage());
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(apiResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("IllegalArgumentException at [{}]: {}", getRequestPath(request), ex.getMessage());
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("MethodArgumentNotValidException at [{}]: {}", getRequestPath(request), ex.getMessage());
        // Extract validation error messages
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // Use the detailed error message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(errors, HttpStatus.BAD_REQUEST.value(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unhandled exception at [{}]: {}", getRequestPath(request), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>("An unexpected error occurred: " + ex.getMessage(), 
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    /**
     * Extract request path from WebRequest
     */
    private String getRequestPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }
}

