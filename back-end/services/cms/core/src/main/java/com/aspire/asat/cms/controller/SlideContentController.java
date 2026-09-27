package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.slide.SlideContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface SlideContentController {

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_SLIDE)
    ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> createSlideContent(@RequestBody ContentReqDto<SlideContentDto> slideContentDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_SLIDE + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> updateSlideContent(@PathVariable("id") UUID contentId, @RequestBody ContentReqDto<SlideContentDto> slideContentDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_SLIDE)
    ResponseEntity<ApiResponseDto<List<ContentRespDto<SlideContentDto>>>> getAllSlideContents(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                                                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                                                              @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                                              @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_SLIDE + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> getSlideContentById(@PathVariable("id") UUID contentId);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_SLIDE + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<SlideContentDto>>> deleteSlideContentById(@PathVariable("id") UUID contentId);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_SLIDE + WebApiUrlConstants.PATH_VAR_SEARCH)
    List<ContentRespDto<SlideContentDto>> searchSlideContents(@RequestParam String query);
}
