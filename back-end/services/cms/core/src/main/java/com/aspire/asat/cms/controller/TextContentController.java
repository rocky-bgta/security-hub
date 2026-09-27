package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.TextContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface TextContentController {

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_TEXT)
    ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> createTextContent(@RequestBody ContentReqDto<TextContentDto> textContentDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_TEXT + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> updateTextContent(@PathVariable("id") UUID contentId, @RequestBody ContentReqDto<TextContentDto> textContentDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_TEXT)
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ContentRespDto<TextContentDto>>>>> getAllTextContents(
                @RequestParam(value = "search", required = false) String search,
                @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_TEXT + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> getTextContentById(@PathVariable("id") UUID contentId);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_TEXT + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<TextContentDto>>> deleteTextContentById(@PathVariable("id") UUID contentId);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_TEXT + WebApiUrlConstants.PATH_VAR_SEARCH)
    List<ContentRespDto<TextContentDto>> searchTextContents(@RequestParam String query);



}
