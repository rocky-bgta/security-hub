package com.aspire.asat.cms.dto.apiResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

