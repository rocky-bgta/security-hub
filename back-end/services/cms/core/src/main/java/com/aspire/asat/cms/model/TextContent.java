package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.TextContentDto;
import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextContent {

    private String id;
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;

    private TextContentDto specificContent;

//    private String title;
//    private String subtitle;
//    private String paragraph;
//    private TextFormatting textFormatting;
//    private TextBackgroundSettingsDto textBackgroundSettings;
//    private boolean highContrastModeEnabled;

    private Instant createdAt;
    private Instant updatedAt;

    public static TextContent toTextContent(String id, ContentReqDto<TextContentDto> dto, Instant createdAt, Instant updatedAt) {
        return TextContent.builder()
                .id(id)
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .specificContent(dto.getSpecific())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static TextContent toUpdateTextContent(TextContent textContent, ContentReqDto<TextContentDto> dto, Instant updatedAt) {
        return TextContent.builder()
                .id(textContent.getId())
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .specificContent(dto.getSpecific())
                .createdAt(textContent.getCreatedAt())
                .updatedAt(updatedAt)
                .build();
    }

    public static ContentRespDto<TextContentDto> toContentRespDto(TextContent textContent) {
        return ContentRespDto.<TextContentDto>builder()
                .id(textContent.getId())
                .common(ContentCommonDto.builder()
                        .contentName(textContent.getContentName())
                        .description(textContent.getDescription())
                        .contentType(textContent.getContentType())
                        .status(textContent.getStatus())
                        .author(textContent.getAuthor())
                        .chapterIds(textContent.getChapterIds())
                        .tags(textContent.getTags())
                        .build())
                .specific(textContent.getSpecificContent())
                .createdAt(textContent.getCreatedAt())
                .updatedAt(textContent.getUpdatedAt())
                .build();
    }

}
