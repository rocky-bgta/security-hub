package com.aspire.asat.vps.dto.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private int statusCode;
    private T data;

    public ApiResponse(String message, int statusCode, T data) {
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }
}
