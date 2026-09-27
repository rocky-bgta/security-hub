package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.tag.TagReqDto;
import com.aspire.asat.cms.dto.tag.TagRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private Instant createdAt;

    private String createdBy;

    private String status;

    public static Tag toEntity(TagReqDto request, String createdBy) {
        Instant now = Instant.now();
        return Tag.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .description(request.getDescription())
                .createdAt(now)
                .createdBy(createdBy)
                .status(STATUS_ACTIVE)
                .build();
    }

    public static TagRespDto toRespDto(Tag tag) {
        return TagRespDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .createdBy(tag.getCreatedBy())
                .status(tag.getStatus())
                .build();
    }
}
