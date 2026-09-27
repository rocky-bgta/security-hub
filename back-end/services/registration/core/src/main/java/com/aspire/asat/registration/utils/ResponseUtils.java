package com.aspire.asat.registration.utils;


import com.aspire.asat.registration.data.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class ResponseUtils {

    public static <T> ResponseEntity<ApiResponse<T>> buildSuccessResponse(T data, String message, String requestUri, LocalDateTime startTime) {
        long responseTimeMillis = Duration.between(startTime, LocalDateTime.now()).toMillis();
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message(message)
                        .data(data)
                        .build()
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(String message, String requestUri, LocalDateTime startTime, HttpStatus status) {
        long responseTimeMillis = Duration.between(startTime, LocalDateTime.now()).toMillis();
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .statusCode(status.value())
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(String message, Map<String, String> errors, String requestUri, LocalDateTime startTime, HttpStatus status) {
        long responseTimeMillis = Duration.between(startTime, LocalDateTime.now()).toMillis();
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .statusCode(status.value())
                        .message(message)
                        .data((T) errors) // Casting errors to T because data is generic
                        .build()
        );
    }

}
