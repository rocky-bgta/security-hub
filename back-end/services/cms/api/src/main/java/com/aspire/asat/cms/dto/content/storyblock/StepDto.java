package com.aspire.asat.cms.dto.content.storyblock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepDto {
    List<StoryDto> stories;
    private String stepTitle;
    private String stepDescription;
    private String textFormatting;
    private Map<String, String> additionalProperties;
    private Map<String, Object> metadata;
}
