package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request DTO for creating a question.")
public class QuestionRequestDto {
    @Schema(description = "The ID of the topic associated with the question", example = "topic-001")
    @NotBlank(message = "Topic ID must not be blank")
    private String topicId;

    @Schema(description = "The type of the question", example = "MULTIPLE_CHOICE")
    @NotNull(message = "Question type must not be null")
    private QuestionTypes questionType;

    @Schema(description = "The text of the question", example = "What is phishing?")
    @NotBlank(message = "Question text must not be blank")
    private String questionText;

    private QuestionStatus status;

    @NotNull(message = "Options cannot be null.")
    private List<QuestionOptionDto> options;
}
