package com.aspire.asat.auth.dto.apiResponses.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String error;
    private String message;
    private int statusCode;
    private String path;
    private LocalDateTime timestamp;
    private String errorCode;
    private Integer retryAfterSeconds;

    public ErrorResponseDTO(String error, String message, int statusCode, String path, LocalDateTime timestamp) {
        this.error = error;
        this.message = message;
        this.statusCode = statusCode;
        this.path = path;
        this.timestamp = timestamp;
    }
}
