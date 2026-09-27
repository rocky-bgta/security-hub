package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.ContentDto;
import com.aspire.asat.registration.data.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;


@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Content {

    @Id
    private UUID id;
    private String name;
    private ContentType type;

    public static ContentDto toContentDto(Content content) {
        return ContentDto.builder().
                id(content.getId()).
                name(content.getName()).
                type(content.getType()).
                build();
    }

    public static Content toContent(ContentDto saveContentDto) {
        return Content.builder().
                id(UUID.randomUUID()).
                name(saveContentDto.getName()).
                type(saveContentDto.getType()).
                build();
    }

    public static Content toUpdateContent(ContentDto contentDto) {
        return Content.builder().
                id(contentDto.getId()).
                name(contentDto.getName()).
                type(contentDto.getType()).
                build();
    }

}