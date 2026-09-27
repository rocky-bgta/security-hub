package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.TextContentDto;

import java.util.List;
import java.util.UUID;

public interface TextContentService {

    ContentRespDto<TextContentDto> createTextContent(ContentReqDto<TextContentDto> textContentDto);

    ContentRespDto<TextContentDto> updateTextContent(UUID contentId, ContentReqDto<TextContentDto> textContentDto);

    List<ContentRespDto<TextContentDto>> getAllTextContents(String search, Integer offset, Integer pageSize, String sortBy, String order);

    ContentRespDto<TextContentDto> getTextContentById(UUID contentId);

    ContentRespDto<TextContentDto> deleteTextContentById(UUID contentId);

    List<ContentRespDto<TextContentDto>> searchTextContents(String searchQuery);

    long getTotalTextContentCount();

}
