package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicMinimalDto {
    private String topicId; // Topic ID for reference
    private String topicName;
    private String description;
    private Integer durationMinutes;
    private String thumbnail;
    private String contentType;
    private List<String> category;
}

