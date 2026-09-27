package com.aspire.asat.registration.data.apiResponses;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString
@NoArgsConstructor // <-- REQUIRED by Jackson
@Builder // Optional if you're using builder
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

