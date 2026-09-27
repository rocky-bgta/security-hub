package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SMTP connection test results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDto {

    private boolean success;
    private String message;
    private long responseTimeMs;
    private String serverResponse;
}
