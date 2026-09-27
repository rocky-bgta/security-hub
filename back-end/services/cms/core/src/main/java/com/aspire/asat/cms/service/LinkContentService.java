package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.LinkContentDto;

import java.util.List;
import java.util.UUID;

public interface LinkContentService {

    ContentRespDto<LinkContentDto> createLinkContent(ContentReqDto<LinkContentDto> linkContentDto);

    ContentRespDto<LinkContentDto> updateLinkContent(UUID contentId, ContentReqDto<LinkContentDto> linkContentDto);

    List<ContentRespDto<LinkContentDto>> getAllLinkContents(String search, Integer offset, Integer pageSize, String sortBy, String order);

    ContentRespDto<LinkContentDto> getLinkContentById(UUID contentId);

    ContentRespDto<LinkContentDto> deleteLinkContentById(UUID contentId);

    long getTotalLinkCount();
}
