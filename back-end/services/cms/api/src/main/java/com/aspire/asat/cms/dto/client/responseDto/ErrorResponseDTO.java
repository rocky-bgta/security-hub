package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private String error;
    private String message;
    private int statusCode;
    private String path;
    private LocalDateTime timestamp;
}
