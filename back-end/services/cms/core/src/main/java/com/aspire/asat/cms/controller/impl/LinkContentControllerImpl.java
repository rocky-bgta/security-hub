package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.LinkContentController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.LinkContentDto;
import com.aspire.asat.cms.service.LinkContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class LinkContentControllerImpl implements LinkContentController {

    private final LinkContentService linkContentService;


    @Autowired
    public  LinkContentControllerImpl(LinkContentService linkContentService) {
        this.linkContentService = linkContentService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> createLinkContent(ContentReqDto<LinkContentDto> linkContentDto) {
        ContentRespDto<LinkContentDto> savedLinkContent = linkContentService.createLinkContent(linkContentDto);
        ApiResponseDto<ContentRespDto<LinkContentDto>> response = new ApiResponseDto<>("Link content created successfully", HttpStatus.CREATED.value(), savedLinkContent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> updateLinkContent(UUID contentId, ContentReqDto<LinkContentDto> linkContentDto) {
        ContentRespDto<LinkContentDto> updatedLinkContent = linkContentService.updateLinkContent(contentId, linkContentDto);
        ApiResponseDto<ContentRespDto<LinkContentDto>> response = new ApiResponseDto<>("Link content updated successfully", HttpStatus.OK.value(), updatedLinkContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ContentRespDto<LinkContentDto>>>>> getAllLinkContents(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        List<ContentRespDto<LinkContentDto>> linkContents = linkContentService.getAllLinkContents(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<ContentRespDto<LinkContentDto>>> allResponseDto = new AllResponseDto<>(offset, pageSize, linkContentService.getTotalLinkCount(), linkContents);
        ApiResponseDto<AllResponseDto<List<ContentRespDto<LinkContentDto>>>> response = new ApiResponseDto<>("Link contents fetched successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> getLinkContentById(UUID contentId) {
        ContentRespDto<LinkContentDto> linkContent = linkContentService.getLinkContentById(contentId);
        ApiResponseDto<ContentRespDto<LinkContentDto>> response = new ApiResponseDto<>("Link content fetched successfully", HttpStatus.OK.value(), linkContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> deleteLinkContentById(UUID contentId) {
        ContentRespDto<LinkContentDto> deletedLinkContent = linkContentService.deleteLinkContentById(contentId);
        ApiResponseDto<ContentRespDto<LinkContentDto>> response = new ApiResponseDto<>("Link content deleted successfully", HttpStatus.OK.value(), deletedLinkContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
