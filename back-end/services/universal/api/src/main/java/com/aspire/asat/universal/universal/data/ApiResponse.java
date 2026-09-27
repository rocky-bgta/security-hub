package com.aspire.asat.universal.universal.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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

