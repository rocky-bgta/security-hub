package com.aspire.asat.billing.dto.apiResponses;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

