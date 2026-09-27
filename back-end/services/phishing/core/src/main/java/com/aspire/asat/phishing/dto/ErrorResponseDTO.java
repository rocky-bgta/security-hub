package com.aspire.asat.phishing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String errorType;
    private String message;
    private int statusCode;
    private String path;
    private LocalDateTime timestamp;
}

