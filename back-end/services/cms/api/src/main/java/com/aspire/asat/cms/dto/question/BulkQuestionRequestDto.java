package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating multiple questions in bulk.")
public class BulkQuestionRequestDto {

    @Schema(description = "List of questions to be created", example = "List of QuestionRequestDto")
    @NotEmpty(message = "Questions list must not be empty")
    @NotNull(message = "Questions list must not be null")
    @Valid
    private List<QuestionRequestDto> questions;
}
