package com.aspire.asat.notification.dto;

import lombok.Data;

@Data
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
