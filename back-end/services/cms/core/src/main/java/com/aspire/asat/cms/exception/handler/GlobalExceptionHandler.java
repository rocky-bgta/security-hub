package com.aspire.asat.cms.exception.handler;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.ErrorResponseDTO;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.exception.ResourceUpdateNotAllowedException;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CmsServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleCmsServiceException(CmsServiceException ex, HttpServletRequest request) {
        log.warn("CmsServiceException at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("CMS Error", ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ErrorResponseDTO> handleAspireException(AspireException ex, HttpServletRequest request) {
        log.warn("AspireException at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Aspire Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // Handles ResourceNotFoundException and returns a structured response
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(apiResponse);
    }

    // Handles ResourceUpdateNotAllowedException and returns a structured response
    @ExceptionHandler(ResourceUpdateNotAllowedException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceUpdateNotAllowed(ResourceUpdateNotAllowedException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_IMPLEMENTED.value(), null);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(apiResponse);
    }

    // Handles ResourceNotFoundException and returns a structured response
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.CONFLICT.value(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }
    @ExceptionHandler(DuplicateDataFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleDuplicateDataFound(DuplicateDataFoundException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.CONFLICT.value(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("timestamp", Instant.now());
//        String message = "Validation Error";
//        String errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(x -> x.getDefaultMessage())
//                .collect(Collectors.joining(", "));
//
//        body.put("error", errors);
//        body.put("path", request.getDescription(false).replace("uri=", ""));
//
//        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(message, HttpStatus.BAD_REQUEST.value(), body);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(apiResponse);
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation error at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst().orElse("Constraint violation");
        log.warn("Constraint violation at [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse("Validation Error", message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Unexpected Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String errorType, String message, HttpStatus status, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ErrorResponseDTO(errorType, message, status.value(), request.getRequestURI(), LocalDateTime.now())
        );
    }
}
