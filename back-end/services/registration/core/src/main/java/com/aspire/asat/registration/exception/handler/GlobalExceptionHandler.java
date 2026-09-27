package com.aspire.asat.registration.exception.handler;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.registration.data.trial.response.ExistingUsersResponseDto;
import com.aspire.asat.registration.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.exception.ServiceException;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.OrganizationTypeAlreadyExistsException;
import com.aspire.asat.registration.exception.OrganizationTypeNotFoundException;
import com.aspire.asat.registration.exception.PackageNotFoundException;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.exception.DomainConflictException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.exception.CustomException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ToastMessageResolver toastMessageResolver;

    private String resolve(String message) {
        return toastMessageResolver.resolve(message);
    }

    @ExceptionHandler(AspireException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAspireException(AspireException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleServiceException(ServiceException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiResponseDto<>(resolve(ex.getMessage()), ex.getStatus().value(), null));
    }


    @ExceptionHandler(RegistrationServiceException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRegistrationException(RegistrationServiceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.BAD_REQUEST.value(), null));
    }

    @ExceptionHandler(PackageNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePackageNotFound(PackageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null));
    }

    // Handles ResourceNotFoundException and returns a structured response
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.NOT_FOUND.value(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(apiResponse);
    }

    // Handles ResourceAlreadyExistsException and returns a structured response
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.CONFLICT.value(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }

    /**
     * Common-module duplicate resource (e.g. client admin email) — same HTTP semantics as registration ResourceAlreadyExistsException.
     */
    @ExceptionHandler(com.aspire.asat.common.exception.ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleCommonResourceAlreadyExists(
            com.aspire.asat.common.exception.ResourceAlreadyExistsException ex, WebRequest request) {
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.CONFLICT.value(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }

    // Handles DomainConflictException and returns existing users info for pop-up display
    @ExceptionHandler(DomainConflictException.class)
    public ResponseEntity<ApiResponseDto<ExistingUsersResponseDto>>
            handleDomainConflict(DomainConflictException ex, WebRequest request) {
        ApiResponseDto<ExistingUsersResponseDto> apiResponse =
                new ApiResponseDto<>(
                    ex.getMessage(), 
                    HttpStatus.CONFLICT.value(), 
                    ex.getExistingUsersInfo()
                );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }

    @ExceptionHandler(DuplicateDataFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleDuplicateDataFound(DuplicateDataFoundException ex, WebRequest request) {
        // Use the exception message in the message field, set data to null
        ApiResponseDto<Object> apiResponse = new ApiResponseDto<>(resolve(ex.getMessage()), HttpStatus.CONFLICT.value(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(apiResponse);
    }

    @ExceptionHandler(OrganizationTypeNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleOrganizationTypeNotFound(OrganizationTypeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null));
    }

    @ExceptionHandler(OrganizationTypeAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleOrganizationTypeAlreadyExists(OrganizationTypeAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.CONFLICT.value(), null));
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

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCustomException(CustomException ex) {
        // If the cause is a known exception, use its message
        Throwable cause = ex.getCause();
        String message = (cause != null && cause instanceof RegistationValidationException) 
            ? cause.getMessage() 
            : ex.getMessage();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(message, HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(toastMessageResolver.resolve(MessageKeys.SYSTEM_UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }
}
