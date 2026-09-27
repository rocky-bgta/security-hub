package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.SlideContentController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.slide.SlideContentDto;
import com.aspire.asat.cms.service.SlideContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SlideContentControllerImpl implements SlideContentController {

    private final SlideContentService slideContentService;

    @Autowired
    public SlideContentControllerImpl(SlideContentService slideContentService) {
        this.slideContentService = slideContentService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> createSlideContent(ContentReqDto<SlideContentDto> slideContentDto) {
        ContentRespDto<SlideContentDto> savedSlideContent = slideContentService.createSlideContent(slideContentDto);
        ApiResponseDto<ContentRespDto<SlideContentDto>> response = new ApiResponseDto<>("Slide content created successfully", 201, savedSlideContent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> updateSlideContent(UUID contentId, ContentReqDto<SlideContentDto> slideContentDto) {
        ContentRespDto<SlideContentDto> updatedSlideContent = slideContentService.updateSlideContent(contentId, slideContentDto);
        ApiResponseDto<ContentRespDto<SlideContentDto>> response = new ApiResponseDto<>("Slide content updated successfully", 200, updatedSlideContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ContentRespDto<SlideContentDto>>>> getAllSlideContents(Integer offset, Integer pageSize, String sortBy, String order) {
        List<ContentRespDto<SlideContentDto>> slideContents = slideContentService.getAllSlideContents(offset, pageSize, sortBy, order);
        ApiResponseDto<List<ContentRespDto<SlideContentDto>>> response = new ApiResponseDto<>("Slide contents fetched successfully", 200, slideContents);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> getSlideContentById(UUID contentId) {
        ContentRespDto<SlideContentDto> slideContent = slideContentService.getSlideContentById(contentId);
        ApiResponseDto<ContentRespDto<SlideContentDto>> response = new ApiResponseDto<>("Slide content fetched successfully", 200, slideContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> deleteSlideContentById(UUID contentId) {
        ContentRespDto<SlideContentDto> deletedSlideContent = slideContentService.deleteSlideContentById(contentId);
        ApiResponseDto<ContentRespDto<SlideContentDto>> response = new ApiResponseDto<>("Slide content deleted successfully", 200, deletedSlideContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<ContentRespDto<SlideContentDto>> searchSlideContents(String query) {
        return slideContentService.searchSlideContents(query);
    }
}
