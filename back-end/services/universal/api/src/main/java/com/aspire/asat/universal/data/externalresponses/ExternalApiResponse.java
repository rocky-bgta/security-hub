package com.aspire.asat.universal.data.externalresponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiResponse<T> {
    private String message;
    private Integer statusCode;
    private T data;
}

