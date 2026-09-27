package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for EmailType.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTypeDto {

    private String id;
    private String name;
    private String description;
    private Boolean isActive;
    private Instant createdAt;
}

