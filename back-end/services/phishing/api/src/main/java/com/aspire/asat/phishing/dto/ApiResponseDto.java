package com.aspire.asat.phishing.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper for all endpoints.
 */
@Data
@NoArgsConstructor
@Builder
public class ApiResponseDto<T> {
    private String message;
    private int statusCode;
    private T data;

    public ApiResponseDto(String message, int statusCode, T data) {
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }
}

