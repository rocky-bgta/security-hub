package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.storyblock.StoryBlockContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface StoryBlockContentController {

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_STORY_BLOCK)
    ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> createStoryBlockContent(@RequestBody ContentReqDto<StoryBlockContentDto> storyBlockContentDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_STORY_BLOCK + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> updateStoryBlockContent(@PathVariable("id") UUID contentId, @RequestBody ContentReqDto<StoryBlockContentDto> storyBlockContentDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_STORY_BLOCK)
    ResponseEntity<ApiResponseDto<List<ContentRespDto<StoryBlockContentDto>>>> getAllStoryBlockContents(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                                                                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                                                                        @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                                                        @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_STORY_BLOCK + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> getStoryBlockContentById(@PathVariable("id") UUID contentId);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_STORY_BLOCK + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<StoryBlockContentDto>>> deleteStoryBlockContentById(@PathVariable("id") UUID contentId);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_STORY_BLOCK + WebApiUrlConstants.PATH_VAR_SEARCH)
    List<ContentRespDto<StoryBlockContentDto>> searchStoryBlockContents(@RequestParam String query);
}
