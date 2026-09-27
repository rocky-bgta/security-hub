package com.aspire.asat.billing.exception.handler;

import com.aspire.asat.billing.dto.ErrorResponseDTO;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.CouponValidationException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.common.exception.AspireException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BillingServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleBillingException(
            BillingServiceException ex, HttpServletRequest request) {

        log.warn("BillingServiceException at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildErrorResponse("Billing Error", ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("ResourceNotFoundException at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null));
    }

    @ExceptionHandler(com.aspire.asat.billing.exception.ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceAlreadyExistsException(
            com.aspire.asat.billing.exception.ResourceAlreadyExistsException ex, HttpServletRequest request) {

        log.warn("ResourceAlreadyExistsException at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.CONFLICT.value(), null));
    }

    @ExceptionHandler(CouponValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleCouponValidationException(
            CouponValidationException ex, HttpServletRequest request) {

        log.warn("CouponValidationException at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse("Coupon Validation Error", ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ErrorResponseDTO> handleAspireException(
            AspireException ex, HttpServletRequest request) {

        log.warn("AspireException at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildErrorResponse("Aspire Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation error at [{}]: {}", request.getRequestURI(), message);

        return buildErrorResponse("Validation Error", message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {

        log.warn("No handler for [{}]: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>("Resource not found", HttpStatus.NOT_FOUND.value(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildErrorResponse("Unexpected Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String errorType, String message, HttpStatus status, HttpServletRequest request) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                errorType,
                message,
                status.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(response);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Constraint violation at [{}]: {}", request.getRequestURI(), message);

        ErrorResponseDTO response = new ErrorResponseDTO(
                "Validation Error",
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
