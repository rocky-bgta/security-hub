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
@Schema(description = "Response DTO for bulk question creation.")
public class BulkQuestionResponseDto {

    @Schema(description = "List of successfully created questions", example = "List of QuestionResponseDto")
    private List<QuestionResponseDto> createdQuestions;

    @Schema(description = "Total number of questions created", example = "5")
    private Integer totalCreated;

    @Schema(description = "Number of questions that failed to create", example = "0")
    private Integer failedCount;

    @Schema(description = "List of error messages for failed questions", example = "List of error messages")
    private List<String> errors;
}
