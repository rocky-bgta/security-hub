package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Row-level error details for sender profile bulk import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderProfileImportErrorDto {
    private int rowNumber;
    private String profileName;
    private String message;
}
