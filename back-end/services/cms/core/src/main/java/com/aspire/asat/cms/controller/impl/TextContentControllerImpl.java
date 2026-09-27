package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.TextContentController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.TextContentDto;
import com.aspire.asat.cms.service.TextContentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TextContentControllerImpl implements TextContentController {

    private final TextContentService textContentService;


    @Autowired
    public TextContentControllerImpl(TextContentService textContentService) {
        this.textContentService = textContentService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> createTextContent(ContentReqDto<TextContentDto> textContentDto) {
        ContentRespDto<TextContentDto> savedTextContent = textContentService.createTextContent(textContentDto);
        ApiResponseDto<ContentRespDto<TextContentDto>> response = new ApiResponseDto<>("Text content created successfully", HttpStatus.CREATED.value(), savedTextContent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> updateTextContent(UUID contentId, ContentReqDto<TextContentDto> textContentDto) {
        ContentRespDto<TextContentDto> updatedTextContent = textContentService.updateTextContent(contentId, textContentDto);
        ApiResponseDto<ContentRespDto<TextContentDto>> response = new ApiResponseDto<>("Text content updated successfully", HttpStatus.OK.value(), updatedTextContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ContentRespDto<TextContentDto>>>>> getAllTextContents(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        List<ContentRespDto<TextContentDto>> textContents = textContentService.getAllTextContents(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<ContentRespDto<TextContentDto>>> allResponseDto = new AllResponseDto<>(offset, pageSize, textContentService.getTotalTextContentCount(), textContents);
        ApiResponseDto<AllResponseDto<List<ContentRespDto<TextContentDto>>>> response = new ApiResponseDto<>("Text contents fetched successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> getTextContentById(UUID contentId) {
        ContentRespDto<TextContentDto> textContent = textContentService.getTextContentById(contentId);
        ApiResponseDto<ContentRespDto<TextContentDto>> response = new ApiResponseDto<>("Text content fetched successfully", HttpStatus.OK.value(), textContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> deleteTextContentById(UUID contentId) {
        ContentRespDto<TextContentDto> deletedTextContent = textContentService.deleteTextContentById(contentId);
        ApiResponseDto<ContentRespDto<TextContentDto>> response = new ApiResponseDto<>("Text content deleted successfully", HttpStatus.OK.value(), deletedTextContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<ContentRespDto<TextContentDto>> searchTextContents(String query) {
        return textContentService.searchTextContents(query);
    }
}
