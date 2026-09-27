package com.aspire.asat.cms.dto.content.storyblock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryDto {
    private String featureImageUrl;
    private String storyDescription;
    private Map<String, String> additionalProperties;
    private Map<String, Object> metadata;
}
