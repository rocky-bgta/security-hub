package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.PdfContentDto;
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
public class PdfContent {
    private String id;
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;
    private String pdfTitle;
    private String pdfFileName;
    private String pdfFileURL;
    private Instant createdAt;
    private Instant updatedAt;

    public static PdfContent toPdfContent(String id, ContentReqDto<PdfContentDto> dto, Instant createdAt, Instant updatedAt) {
        return PdfContent.builder()
                .id(id)
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                //.pdfTitle(dto.getSpecific().getPdfTitle())
                .pdfFileName(dto.getSpecific().getPdfFileName())
                .pdfFileURL(dto.getSpecific().getPdfFileURL())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static PdfContent toUpdatePdfContent(PdfContent pdfContent, ContentReqDto<PdfContentDto> dto, Instant updatedAt) {
        return PdfContent.builder()
                .id(pdfContent.getId())
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                //.pdfTitle(dto.getSpecific().getPdfTitle())
                .pdfFileName(dto.getSpecific().getPdfFileName())
                .pdfFileURL(dto.getSpecific().getPdfFileURL())
                .createdAt(pdfContent.getCreatedAt())
                .updatedAt(updatedAt)
                .build();
    }

    public static ContentRespDto<PdfContentDto> toContentRespDto(PdfContent pdfContent) {
        return ContentRespDto.<PdfContentDto>builder()
                .id(pdfContent.getId())
                .common(ContentCommonDto.builder()
                        .contentName(pdfContent.getContentName())
                        .description(pdfContent.getDescription())
                        .contentType(pdfContent.getContentType())
                        .status(pdfContent.getStatus())
                        .author(pdfContent.getAuthor())
                        .chapterIds(pdfContent.getChapterIds())
                        .tags(pdfContent.getTags())
                        .build())
                .specific(PdfContentDto.builder()
                        //.pdfTitle(pdfContent.getPdfTitle())
                        .pdfFileName(pdfContent.getPdfFileName())
                        .pdfFileURL(pdfContent.getPdfFileURL())
                        .build())
                .build();
    }
}
