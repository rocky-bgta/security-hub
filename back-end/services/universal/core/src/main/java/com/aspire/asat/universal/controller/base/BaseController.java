package com.aspire.asat.universal.controller.base;


import com.aspire.asat.universal.universal.data.enums.ResponseMessage;
import com.aspire.asat.universal.service.base.LocaleMessageService;
import com.aspire.asat.universal.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.aspire.asat.universal.universal.data.ApiResponse;

import java.time.LocalDateTime;

public abstract class BaseController {

    private LocaleMessageService localeMessageService;

    @Autowired
    public void setLocaleMessageService(LocaleMessageService localeMessageService) {
        this.localeMessageService = localeMessageService;
    }

    protected <T> ResponseEntity<ApiResponse<T>> handleRequest(
            ServiceMethod<T> serviceMethod,
            String successMessage,
            HttpServletRequest request) {

        LocalDateTime startTime = LocalDateTime.now();
        try {
            T result = serviceMethod.execute();
            return ResponseUtils.buildSuccessResponse(result, successMessage, request.getRequestURI(), startTime);
        } catch (Exception e) {
            String errorMessage = resolveMessage(e.getMessage());
            return ResponseUtils.buildErrorResponse("Error: " + errorMessage, request.getRequestURI(), startTime, HttpStatus.BAD_REQUEST);
        }
    }

    protected ResponseEntity<ApiResponse<Void>> handleRequest(
            Runnable serviceMethod,
            String successMessage,
            HttpServletRequest request) {

        LocalDateTime startTime = LocalDateTime.now();
        try {
            serviceMethod.run();
            return ResponseUtils.buildSuccessResponse(null, successMessage, request.getRequestURI(), startTime);
        } catch (Exception e) {
            String errorMessage = resolveMessage(e.getMessage());
            return ResponseUtils.buildErrorResponse("Error: " + errorMessage, request.getRequestURI(), startTime, HttpStatus.BAD_REQUEST);
        }
    }

    protected String getMessage(ResponseMessage key) {
        return localeMessageService.getLocalMessage(key);
    }

    protected String getMessage(String key) {
        return localeMessageService.getLocalMessage(key);
    }

    // Helper: Fallback to original message if key not found
    private String resolveMessage(String keyOrMessage) {
        try {
            return localeMessageService.getLocalMessage(keyOrMessage);
        } catch (Exception ex) {
            return keyOrMessage; // fallback to raw message
        }
    }

    @FunctionalInterface
    protected interface ServiceMethod<T> {
        T execute() throws Exception;
    }
}
