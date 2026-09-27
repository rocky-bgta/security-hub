package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.StoryBlockContentController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.storyblock.StoryBlockContentDto;
import com.aspire.asat.cms.service.StoryBlockContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class StoryBlockContentControllerImpl implements StoryBlockContentController {
    private final StoryBlockContentService storyBlockContentService;

    @Autowired
    public StoryBlockContentControllerImpl(StoryBlockContentService storyBlockContentService) {
        this.storyBlockContentService = storyBlockContentService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> createStoryBlockContent(ContentReqDto<StoryBlockContentDto> storyBlockContentDto) {
        ContentRespDto<StoryBlockContentDto> savedStoryBlockContent = storyBlockContentService.createStoryBlockContent(storyBlockContentDto);
        ApiResponseDto<ContentRespDto<StoryBlockContentDto>> response = new ApiResponseDto<>("Story block content created successfully", 201, savedStoryBlockContent);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> updateStoryBlockContent(UUID contentId, ContentReqDto<StoryBlockContentDto> storyBlockContentDto) {
        ContentRespDto<StoryBlockContentDto> updatedStoryBlockContent = storyBlockContentService.updateStoryBlockContent(contentId, storyBlockContentDto);
        ApiResponseDto<ContentRespDto<StoryBlockContentDto>> response = new ApiResponseDto<>("Story block content updated successfully", 200, updatedStoryBlockContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ContentRespDto<StoryBlockContentDto>>>> getAllStoryBlockContents(Integer offset, Integer pageSize, String sortBy, String order) {
        List<ContentRespDto<StoryBlockContentDto>> storyBlockContents = storyBlockContentService.getAllStoryBlockContents(offset, pageSize, sortBy, order);
        ApiResponseDto<List<ContentRespDto<StoryBlockContentDto>>> response = new ApiResponseDto<>("Story block contents fetched successfully", 200, storyBlockContents);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> getStoryBlockContentById(UUID contentId) {
        ContentRespDto<StoryBlockContentDto> storyBlockContent = storyBlockContentService.getStoryBlockContentById(contentId);
        ApiResponseDto<ContentRespDto<StoryBlockContentDto>> response = new ApiResponseDto<>("Story block content fetched successfully", 200, storyBlockContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> deleteStoryBlockContentById(UUID contentId) {
        ContentRespDto<StoryBlockContentDto> deletedStoryBlockContent = storyBlockContentService.deleteStoryBlockContentById(contentId);
        ApiResponseDto<ContentRespDto<StoryBlockContentDto>> response = new ApiResponseDto<>("Story block content deleted successfully", 200, deletedStoryBlockContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<ContentRespDto<StoryBlockContentDto>> searchStoryBlockContents(String query) {
        return storyBlockContentService.searchStoryBlockContents(query);
    }
}
