package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.slide.SlideContentDto;

import java.util.List;
import java.util.UUID;

public interface SlideContentService {

    ContentRespDto<SlideContentDto> createSlideContent(ContentReqDto<SlideContentDto> slideContentDto);

    ContentRespDto<SlideContentDto> updateSlideContent(UUID contentId, ContentReqDto<SlideContentDto> slideContentDto);

    List<ContentRespDto<SlideContentDto>> getAllSlideContents(Integer offset, Integer pageSize, String sortBy, String order);

    ContentRespDto<SlideContentDto> getSlideContentById(UUID contentId);

    ContentRespDto<SlideContentDto> deleteSlideContentById(UUID contentId);

    List<ContentRespDto<SlideContentDto>> searchSlideContents(String searchQuery);
}
