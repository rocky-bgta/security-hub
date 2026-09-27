package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.content.*;
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
public class LinkContent {

    private String id;
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;

    private LinkContentDto specificContent;

//    private TitleFormatting titleFormatting;
//    private SubTitleFormatting subTitleFormatting;
//    private ParagraphFormatting paragraphFormatting;
//    private LinkFormatting linkFormatting;
//    private BackgroundFormatting backgroundFormatting;
//    private String featureImageLink;

    private Instant createdAt;
    private Instant updatedAt;

//    public static LinkContent toLinkContent(UUID id, ContentReqDto<LinkContentDto> dto, Instant createdAt, Instant updatedAt) {
//        return LinkContent.builder()
//                .id(id)
//                .contentName(dto.getCommon().getContentName())
//                .description(dto.getCommon().getDescription())
//                .contentType(dto.getCommon().getContentType())
//                .status(dto.getCommon().getStatus())
//                .author(dto.getCommon().getAuthor())
//                .chapterIds(dto.getCommon().getChapterIds())
//                .tags(dto.getCommon().getTags())
//                .titleFormatting(dto.getSpecific().getTitleFormatting())
//                .subTitleFormatting(dto.getSpecific().getSubTitleFormatting())
//                .paragraphFormatting(dto.getSpecific().getParagraphFormatting())
//                .linkFormatting(dto.getSpecific().getLinkFormatting())
//                .backgroundFormatting(dto.getSpecific().getBackgroundFormatting())
//                .featureImageLink(dto.getSpecific().getFeatureImageLink())
//                .createdAt(createdAt)
//                .updatedAt(updatedAt)
//                .build();
//    }
//
//    public static LinkContent toUpdateLinkContent(LinkContent linkContent, ContentReqDto<LinkContentDto> dto, Instant updatedAt) {
//        return LinkContent.builder()
//                .id(linkContent.getId())
//                .contentName(dto.getCommon().getContentName())
//                .description(dto.getCommon().getDescription())
//                .contentType(dto.getCommon().getContentType())
//                .status(dto.getCommon().getStatus())
//                .author(dto.getCommon().getAuthor())
//                .chapterIds(dto.getCommon().getChapterIds())
//                .tags(dto.getCommon().getTags())
//                .titleFormatting(dto.getSpecific().getTitleFormatting())
//                .subTitleFormatting(dto.getSpecific().getSubTitleFormatting())
//                .paragraphFormatting(dto.getSpecific().getParagraphFormatting())
//                .linkFormatting(dto.getSpecific().getLinkFormatting())
//                .backgroundFormatting(dto.getSpecific().getBackgroundFormatting())
//                .featureImageLink(dto.getSpecific().getFeatureImageLink())
//                .createdAt(linkContent.getCreatedAt())
//                .updatedAt(updatedAt)
//                .build();
//    }
//
//    public static ContentRespDto<LinkContentDto> toContentRespDto(LinkContent linkContent) {
//        return ContentRespDto.<LinkContentDto>builder()
//                .id(linkContent.getId())
//                .common(ContentCommonDto.builder()
//                        .contentName(linkContent.getContentName())
//                        .description(linkContent.getDescription())
//                        .contentType(linkContent.getContentType())
//                        .status(linkContent.getStatus())
//                        .author(linkContent.getAuthor())
//                        .chapterIds(linkContent.getChapterIds())
//                        .tags(linkContent.getTags())
//                        .build())
//                .specific(LinkContentDto.builder()
//                        .titleFormatting(linkContent.getTitleFormatting())
//                        .subTitleFormatting(linkContent.getSubTitleFormatting())
//                        .paragraphFormatting(linkContent.getParagraphFormatting())
//                        .linkFormatting(linkContent.getLinkFormatting())
//                        .backgroundFormatting(linkContent.getBackgroundFormatting())
//                        .featureImageLink(linkContent.getFeatureImageLink())
//                        .build())
//                .createdAt(linkContent.getCreatedAt())
//                .updatedAt(linkContent.getUpdatedAt())
//                .build();
//    }

    public static LinkContent toLinkContent(String id, ContentReqDto<LinkContentDto> dto, Instant createdAt, Instant updatedAt) {
        return LinkContent.builder()
                .id(id)
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .specificContent(dto.getSpecific()) // Store all formatting inside specificContent
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static LinkContent toUpdateLinkContent(LinkContent linkContent, ContentReqDto<LinkContentDto> dto, Instant updatedAt) {
        return LinkContent.builder()
                .id(linkContent.getId())
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .specificContent(dto.getSpecific()) // Updating the encapsulated DTO
                .createdAt(linkContent.getCreatedAt())
                .updatedAt(updatedAt)
                .build();
    }

    public static ContentRespDto<LinkContentDto> toContentRespDto(LinkContent linkContent) {
        return ContentRespDto.<LinkContentDto>builder()
                .id(linkContent.getId())
                .common(ContentCommonDto.builder()
                        .contentName(linkContent.getContentName())
                        .description(linkContent.getDescription())
                        .contentType(linkContent.getContentType())
                        .status(linkContent.getStatus())
                        .author(linkContent.getAuthor())
                        .chapterIds(linkContent.getChapterIds())
                        .tags(linkContent.getTags())
                        .build())
                .specific(linkContent.getSpecificContent()) // Returning the entire specificContent DTO
                .createdAt(linkContent.getCreatedAt())
                .updatedAt(linkContent.getUpdatedAt())
                .build();
    }

}
