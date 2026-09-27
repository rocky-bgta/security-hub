package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.PdfContentController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.PdfContentDto;
import com.aspire.asat.cms.service.PdfContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class PdfContentControllerImpl implements PdfContentController {
    private final PdfContentService pdfContentService;

    @Autowired
    public PdfContentControllerImpl(PdfContentService pdfContentService) {
        this.pdfContentService = pdfContentService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> createPdfContent(ContentReqDto<PdfContentDto> pdfContentDto) {
        ContentRespDto<PdfContentDto> savedPdfContent = pdfContentService.createPdfContent(pdfContentDto);
        ApiResponseDto<ContentRespDto<PdfContentDto>> response = new ApiResponseDto<>("PDF content created successfully", 201, savedPdfContent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> updatePdfContent(UUID contentId, ContentReqDto<PdfContentDto> pdfContentDto) {
        ContentRespDto<PdfContentDto> updatedPdfContent = pdfContentService.updatePdfContent(contentId, pdfContentDto);
        ApiResponseDto<ContentRespDto<PdfContentDto>> response = new ApiResponseDto<>("PDF content updated successfully", 200, updatedPdfContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ContentRespDto<PdfContentDto>>>> getAllPdfContents(Integer offset, Integer pageSize, String sortBy, String order) {
        List<ContentRespDto<PdfContentDto>> pdfContents = pdfContentService.getAllPdfContents(offset, pageSize, sortBy, order);
        ApiResponseDto<List<ContentRespDto<PdfContentDto>>> response = new ApiResponseDto<>("PDF contents fetched successfully", 200, pdfContents);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> getPdfContentById(UUID contentId) {
        ContentRespDto<PdfContentDto> pdfContent = pdfContentService.getPdfContentById(contentId);
        ApiResponseDto<ContentRespDto<PdfContentDto>> response = new ApiResponseDto<>("PDF content fetched successfully", 200, pdfContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ContentRespDto<PdfContentDto>>> deletePdfContentById(UUID contentId) {
        ContentRespDto<PdfContentDto> deletedPdfContent = pdfContentService.deletePdfContentById(contentId);
        ApiResponseDto<ContentRespDto<PdfContentDto>> response = new ApiResponseDto<>("PDF content deleted successfully", 200, deletedPdfContent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<ContentRespDto<PdfContentDto>> searchPdfContents(String query) {
        return pdfContentService.searchPdfContents(query);
    }
}
