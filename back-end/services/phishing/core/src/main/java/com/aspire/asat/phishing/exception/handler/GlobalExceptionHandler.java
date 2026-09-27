package com.aspire.asat.phishing.exception.handler;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.common.enums.ResponseMessage;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.phishing.dto.ErrorResponseDTO;
import com.aspire.asat.phishing.dto.apiResponses.ApiResponse;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.base.LocaleMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice(basePackages = "com.aspire.asat.phishing")
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final LocaleMessageService localeMessageService;
    private final ToastMessageResolver toastMessageResolver;

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.warn("Phishing Service Exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Phishing Service Error", toastMessageResolver.resolve(ex.getMessage()), ex.getStatus(), request);
    }

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ErrorResponseDTO> handleAspireException(AspireException ex, HttpServletRequest request) {
        log.warn("AspireException at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Aspire Error", localeMessageService.getLocalMessage(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        String message = localeMessageService.getLocalMessage(ResponseMessage.RECORD_NOT_FOUND);
        ApiResponse<Object> apiResponse = new ApiResponse<>(message, HttpStatus.NOT_FOUND.value(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(DuplicateDataFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateDataFound(DuplicateDataFoundException ex, HttpServletRequest request) {
        log.warn("Duplicate data found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse("Conflict Error", toastMessageResolver.resolve(ex.getMessage()), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(PhishingValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handlePhishingValidation(PhishingValidationException ex, HttpServletRequest request) {
        log.warn("Validation error at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse("Validation Error", localeMessageService.getLocalMessage(ex.getMessage()), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation error at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", localeMessageService.getLocalMessage(message), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseDTO> handleBindException(BindException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "Invalid request"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Bind validation error at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", localeMessageService.getLocalMessage(message), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestPart(Exception ex, HttpServletRequest request) {
        log.warn("Missing request part/param at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse("Validation Error", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst().orElse("Constraint violation");
        log.warn("Constraint violation at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", localeMessageService.getLocalMessage(message), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request body at [{}]: {}", request.getRequestURI(), ex.getMessage());
        String message = toastMessageResolver.resolve(MessageKeys.SYSTEM_JSON_PARSE_ERROR);
        return buildErrorResponse("Validation Error", message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Unexpected Error", toastMessageResolver.resolve(MessageKeys.SYSTEM_UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String errorType, String message, HttpStatus status, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ErrorResponseDTO(errorType, message, status.value(), request.getRequestURI(), LocalDateTime.now())
        );
    }
}

