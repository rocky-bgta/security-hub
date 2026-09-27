package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Summary response for sender profile bulk import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderProfileImportResultDto {
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<SenderProfileDto> importedProfiles;
    private List<SenderProfileImportErrorDto> errors;
}
