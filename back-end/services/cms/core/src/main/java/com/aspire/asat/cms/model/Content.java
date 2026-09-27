package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.dto.interactive.ContentData;
import com.aspire.asat.common.service.files.FileService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    private String id;
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;
    private Object specificContent;
    private Instant createdAt;
    private Instant updatedAt;


    public static Content toContent(String id, ContentCommonDto dto,  Instant createdAt, Instant updatedAt, Object specificContent) {
        return Content.builder()
                .id(id)
                .contentName(dto.getContentName())
                .description(dto.getDescription())
                .contentType(dto.getContentType())
                .status(dto.getStatus())
                .author(dto.getAuthor())
                .chapterIds(dto.getChapterIds())
                .tags(dto.getTags())
                .specificContent(specificContent)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Content toUpdateContent(String contentId, ContentCommonDto dto, Object specificContent) {
        return Content.builder()
                .id(contentId)
                .contentName(dto.getContentName())
                .description(dto.getDescription())
                .contentType(dto.getContentType())
                .status(dto.getStatus())
                .author(dto.getAuthor())
                .chapterIds(dto.getChapterIds())
                .tags(dto.getTags())
                .specificContent(specificContent)
                .updatedAt(Instant.now())
                .build();
    }



}
