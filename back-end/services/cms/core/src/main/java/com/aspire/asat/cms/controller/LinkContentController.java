package com.aspire.asat.cms.controller;


import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.LinkContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface LinkContentController {

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_LINK)
    ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> createLinkContent(@Valid @RequestBody ContentReqDto<LinkContentDto> linkContentDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_LINK + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> updateLinkContent(@PathVariable("id") UUID contentId, @RequestBody ContentReqDto<LinkContentDto> linkContentDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_LINK)
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ContentRespDto<LinkContentDto>>>>> getAllLinkContents(@RequestParam(value = "search", required = false) String search,
                                                                                                            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                                                                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                                                                            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                                                            @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_LINK + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> getLinkContentById(@PathVariable("id") UUID contentId);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_LINK+ WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<LinkContentDto>>> deleteLinkContentById(@PathVariable("id") UUID contentId);

}
