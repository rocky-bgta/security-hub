package com.aspire.asat.cms.model.topic;

import com.aspire.asat.cms.dto.topic.ContentTypeReqDto;
import com.aspire.asat.cms.dto.topic.ContentTypeRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "content_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentType {
    private String id;
    private String typeName; // Article, Video, Podcast, etc.
    private String description;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

    public static ContentType toContentType(ContentTypeReqDto dto) {
        return ContentType.builder()
                .id(UUID.randomUUID().toString())
                .typeName(dto.getTypeName())
                .description(dto.getDescription())
                .sortOrder(dto.getSortOrder())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static ContentTypeRespDto contentTypeRespDto(ContentType contentType) {
        return ContentTypeRespDto.builder()
                .id(contentType.getId())
                .typeName(contentType.getTypeName())
                .description(contentType.getDescription())
                .sortOrder(contentType.getSortOrder())
                .createdAt(contentType.getCreatedAt())
                .updatedAt(contentType.getUpdatedAt())
                .build();
    }
}
