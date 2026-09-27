package com.aspire.asat.auth.dto.apiResponses;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

