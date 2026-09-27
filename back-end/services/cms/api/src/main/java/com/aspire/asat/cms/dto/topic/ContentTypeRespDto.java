package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTypeRespDto {
    private String id;
    private String typeName; // Article, Video, Podcast, etc.
    private String description;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
