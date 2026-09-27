package com.aspire.asat.universal.exception.handler;


import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.common.exception.ServiceException;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.exception.DuplicateDataFoundException;
import com.aspire.asat.universal.exception.ResourceNotFoundException;
import com.aspire.asat.universal.exception.UniversalServiceException;
import com.aspire.asat.universal.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.aspire.asat.universal")
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ToastMessageResolver toastMessageResolver;

    private String resolve(String message) {
        return toastMessageResolver.resolve(message);
    }

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAspireException(AspireException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleServiceException(ServiceException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiResponseDto<>(ex.getMessage(), ex.getStatus().value(), null));
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnprocessableEntityException(UnprocessableEntityException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value(), null));
    }

    @ExceptionHandler(UniversalServiceException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRegistrationException(UniversalServiceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.BAD_REQUEST.value(), null));
    }



    // Handles ResourceNotFoundException and returns a structured response
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(apiResponse);
    }

    @ExceptionHandler(DuplicateDataFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleDuplicateDataFound(DuplicateDataFoundException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(ex.getMessage(), HttpStatus.CONFLICT.value(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        // Extract validation error messages
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // Use the detailed error message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(errors, HttpStatus.BAD_REQUEST.value(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(toastMessageResolver.resolve(MessageKeys.SYSTEM_UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }
}
