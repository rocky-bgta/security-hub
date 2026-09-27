package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for question count grouped by topic")
public class QuestionCountByTopicProductDto {

    @Schema(description = "The topic summary with essential fields", example = "TopicSummaryDto")
    private TopicSummaryDto topic;

    @Schema(description = "The count of questions for this topic", example = "15")
    private Long questionCount;

    @Schema(description = "The count of active questions for this topic", example = "12")
    private Long activeQuestionCount;

    @Schema(description = "The count of inactive questions for this topic", example = "3")
    private Long inactiveQuestionCount;

    @Schema(description = "When the topic was created", example = "2024-01-15T10:30:45.123Z")
    private Instant topicCreatedAt;

    @Schema(description = "When the topic was last updated", example = "2024-01-15T10:30:45.123Z")
    private Instant topicUpdatedAt;
}
