package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.PdfContentDto;

import java.util.List;
import java.util.UUID;

public interface PdfContentService {

    ContentRespDto<PdfContentDto> createPdfContent(ContentReqDto<PdfContentDto> pdfContentDto);

    ContentRespDto<PdfContentDto> updatePdfContent(UUID contentId, ContentReqDto<PdfContentDto> pdfContentDto);

    List<ContentRespDto<PdfContentDto>> getAllPdfContents(Integer offset, Integer pageSize, String sortBy, String order);

    ContentRespDto<PdfContentDto> getPdfContentById(UUID contentId);

    ContentRespDto<PdfContentDto> deletePdfContentById(UUID contentId);

    List<ContentRespDto<PdfContentDto>> searchPdfContents(String searchQuery);
}
