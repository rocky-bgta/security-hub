package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.storyblock.StoryBlockContentDto;

import java.util.List;
import java.util.UUID;

public interface StoryBlockContentService {

    ContentRespDto<StoryBlockContentDto> createStoryBlockContent(ContentReqDto<StoryBlockContentDto> storyBlockContentDto);

    ContentRespDto<StoryBlockContentDto> updateStoryBlockContent(UUID contentId, ContentReqDto<StoryBlockContentDto> storyBlockContentDto);

    List<ContentRespDto<StoryBlockContentDto>> getAllStoryBlockContents(Integer offset, Integer pageSize, String sortBy, String order);

    ContentRespDto<StoryBlockContentDto> getStoryBlockContentById(UUID contentId);

    ContentRespDto<StoryBlockContentDto> deleteStoryBlockContentById(UUID contentId);

    List<ContentRespDto<StoryBlockContentDto>> searchStoryBlockContents(String searchQuery);
}
