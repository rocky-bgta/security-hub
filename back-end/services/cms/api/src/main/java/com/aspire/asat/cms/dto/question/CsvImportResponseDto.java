package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for CSV import - returns parsed questions without saving to database")
public class CsvImportResponseDto {

    @Schema(description = "List of parsed questions ready to be saved", example = "List of QuestionRequestDto")
    private List<QuestionRequestDto> questions;

    @Schema(description = "Total number of questions parsed from CSV", example = "10")
    private Integer totalParsed;

    @Schema(description = "Number of questions that failed validation", example = "2")
    private Integer failedCount;

    @Schema(description = "List of validation errors for failed questions", example = "List of error messages with row numbers")
    private List<String> errors;
}

