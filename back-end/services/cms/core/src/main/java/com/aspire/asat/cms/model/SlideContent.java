package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.slide.SlideAttributes;
import com.aspire.asat.cms.dto.content.slide.SlideContentDto;
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
public class SlideContent {
    private String id;
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;
    private List<SlideAttributes> slides;
    private Instant createdAt;
    private Instant updatedAt;

    public static SlideContent toSlideContent(String id, ContentReqDto<SlideContentDto> dto, Instant createdAt, Instant updatedAt) {
        return SlideContent.builder()
                .id(id)
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .slides(dto.getSpecific().getSlides())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static SlideContent toUpdateSlideContent(SlideContent slideContent, ContentReqDto<SlideContentDto> dto, Instant updatedAt) {
        return SlideContent.builder()
                .id(slideContent.getId())
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .slides(dto.getSpecific().getSlides())
                .createdAt(slideContent.getCreatedAt())
                .updatedAt(updatedAt)
                .build();
    }

    public static ContentRespDto<SlideContentDto> toContentRespDto(SlideContent slideContent) {
        return ContentRespDto.<SlideContentDto>builder()
                .common(ContentCommonDto.builder()
                        .contentName(slideContent.getContentName())
                        .description(slideContent.getDescription())
                        .contentType(slideContent.getContentType())
                        .status(slideContent.getStatus())
                        .author(slideContent.getAuthor())
                        .chapterIds(slideContent.getChapterIds())
                        .tags(slideContent.getTags())
                        .build())
                .specific(SlideContentDto.builder()
                        .slides(slideContent.getSlides())
                        .build())
                .build();
    }
}
