package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.PdfContentDto;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.model.PdfContent;
import com.aspire.asat.cms.repository.PdfContentRepository;
import com.aspire.asat.cms.service.PdfContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PdfContentServiceImpl implements PdfContentService {

    private final PdfContentRepository pdfContentRepository;

    @Autowired
    public PdfContentServiceImpl(PdfContentRepository pdfContentRepository) {
        this.pdfContentRepository = pdfContentRepository;
    }

    @Override
    public ContentRespDto<PdfContentDto> createPdfContent(ContentReqDto<PdfContentDto> pdfContentDto) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        if(pdfContentRepository.existsByContentName(pdfContentDto.getCommon().getContentName())){
            throw new RuntimeException("Pdf Content already exists with name: " + pdfContentDto.getCommon().getContentName());
        }
        PdfContent pdfContent = PdfContent.toPdfContent(id.toString(), pdfContentDto, now, now);
        return PdfContent.toContentRespDto(pdfContentRepository.save(pdfContent));
    }

    @Override
    public ContentRespDto<PdfContentDto> updatePdfContent(UUID contentId, ContentReqDto<PdfContentDto> pdfContentDto) {
        return pdfContentRepository.findById(contentId)
                .map(existingPdfContent -> {
                    if(!existingPdfContent.getContentName().equals(pdfContentDto.getCommon().getContentName())){
                        boolean isContentNameExist = pdfContentRepository.existsByContentName(pdfContentDto.getCommon().getContentName());
                        if(isContentNameExist){
                            throw new RuntimeException("Pdf Content already exists with name: " + pdfContentDto.getCommon().getContentName());
                        }
                    }
                    PdfContent updatedPdfContent = PdfContent.toUpdatePdfContent(existingPdfContent, pdfContentDto, Instant.now());
                    return PdfContent.toContentRespDto(pdfContentRepository.save(updatedPdfContent));
                })
                .orElseThrow(() -> new RuntimeException("Pdf Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<PdfContentDto>> getAllPdfContents(Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<PdfContent> pdfContentPage = pdfContentRepository.findByContentType(ContentType.PDF, pageable);
        return pdfContentPage.getContent().stream()
                .map(PdfContent::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentRespDto<PdfContentDto> getPdfContentById(UUID contentId) {
        return pdfContentRepository.findById(contentId)
                .map(PdfContent::toContentRespDto)
                .orElseThrow(() -> new RuntimeException("Pdf Content not found with id: " + contentId));
    }

    @Override
    public ContentRespDto<PdfContentDto> deletePdfContentById(UUID contentId) {
        return pdfContentRepository.findById(contentId)
                .map(pdfContent -> {
                    pdfContentRepository.deleteById(contentId);
                    return PdfContent.toContentRespDto(pdfContent);
                })
                .orElseThrow(() -> new RuntimeException("Pdf Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<PdfContentDto>> searchPdfContents(String searchQuery) {
        return pdfContentRepository.findByContentNameContainingIgnoreCase(searchQuery)
                .stream()
                .map(PdfContent::toContentRespDto)
                .collect(Collectors.toList());
    }
}
