package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.PdfContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.CONTENT_API, produces = "application/json")
public interface PdfContentController {

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_PDF)
    ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> createPdfContent(@RequestBody ContentReqDto<PdfContentDto> pdfContentDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_PDF + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> updatePdfContent(@PathVariable("id") UUID contentId, @RequestBody ContentReqDto<PdfContentDto> pdfContentDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_PDF)
    ResponseEntity<ApiResponseDto<List<ContentRespDto<PdfContentDto>>>> getAllPdfContents(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                                                          @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                                                          @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                                          @RequestParam(value = "order", defaultValue = "desc") String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_PDF + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> getPdfContentById(@PathVariable("id") UUID contentId);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_PDF + WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> deletePdfContentById(@PathVariable("id") UUID contentId);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_CONTENT_PDF + WebApiUrlConstants.PATH_VAR_SEARCH)
    List<ContentRespDto<PdfContentDto>> searchPdfContents(@RequestParam String query);
}
