package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDto {

    @Schema (description = "The text of the question option", example = "An attempt to obtain sensitive information by disguising as a trustworthy entity.")
    @NotNull(message = "Option text must not be null")
    private String optionText;

    @Schema (description = "Indicates if this option is the correct answer", example = "true")
    @NotNull(message = "isCorrect must not be null")
    private Boolean isCorrect;
}
