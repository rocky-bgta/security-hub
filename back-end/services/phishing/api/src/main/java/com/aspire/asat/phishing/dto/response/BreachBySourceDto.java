package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for breach count by source.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachBySourceDto {

    private String source;
    private int count;
    private double percentage;
}
