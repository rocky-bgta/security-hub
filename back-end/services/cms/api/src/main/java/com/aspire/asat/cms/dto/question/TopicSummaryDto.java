package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simplified topic DTO with only essential fields")
public class TopicSummaryDto {

    @Schema(description = "The ID of the topic", example = "topic-001")
    private String topicId;

    @Schema(description = "The name of the topic", example = "Security Awareness")
    private String topicName;

    @Schema(description = "The duration of the topic in minutes", example = "30")
    private Integer durationMinutes;

    @Schema(description = "The description of the topic", example = "Learn about security awareness and best practices")
    private String description;
}
