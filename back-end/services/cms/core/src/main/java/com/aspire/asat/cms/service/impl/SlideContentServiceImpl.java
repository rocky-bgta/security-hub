package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.slide.SlideContentDto;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.model.SlideContent;
import com.aspire.asat.cms.repository.SlideContentRepository;
import com.aspire.asat.cms.service.SlideContentService;
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
public class SlideContentServiceImpl implements SlideContentService {

    private final SlideContentRepository slideContentRepository;

    @Autowired
    public SlideContentServiceImpl(SlideContentRepository slideContentRepository) {
        this.slideContentRepository = slideContentRepository;
    }

    @Override
    public ContentRespDto<SlideContentDto> createSlideContent(ContentReqDto<SlideContentDto> slideContentDto) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        if(slideContentRepository.existsByContentName(slideContentDto.getCommon().getContentName())){
            throw new RuntimeException("Slide Content already exists with name: " + slideContentDto.getCommon().getContentName());
        }

        SlideContent slideContent = SlideContent.toSlideContent(id.toString(), slideContentDto, now, now);
        return SlideContent.toContentRespDto(slideContentRepository.save(slideContent));
    }

    @Override
    public ContentRespDto<SlideContentDto> updateSlideContent(UUID contentId, ContentReqDto<SlideContentDto> slideContentDto) {
        return slideContentRepository.findById(contentId)
                .map(existingSlideContent -> {
                    if(!existingSlideContent.getContentName().equals(slideContentDto.getCommon().getContentName())){
                        boolean isContentNameExist = slideContentRepository.existsByContentName(slideContentDto.getCommon().getContentName());
                        if(isContentNameExist){
                            throw new RuntimeException("Slide Content already exists with name: " + slideContentDto.getCommon().getContentName());
                        }
                    }
                    SlideContent updatedSlideContent = SlideContent.toUpdateSlideContent(existingSlideContent, slideContentDto, Instant.now());
                    return SlideContent.toContentRespDto(slideContentRepository.save(updatedSlideContent));
                })
                .orElseThrow(() -> new RuntimeException("Slide Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<SlideContentDto>> getAllSlideContents(Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<SlideContent> slideContentPage = slideContentRepository.findByContentType(ContentType.SLIDE, pageable);
        return slideContentPage.getContent()
                .stream()
                .map(SlideContent::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentRespDto<SlideContentDto> getSlideContentById(UUID contentId) {
        return slideContentRepository.findById(contentId)
                .map(SlideContent::toContentRespDto)
                .orElseThrow(() -> new RuntimeException("Slide Content not found with id: " + contentId));
    }

    @Override
    public ContentRespDto<SlideContentDto> deleteSlideContentById(UUID contentId) {
        return slideContentRepository.findById(contentId)
                .map(slideContent -> {
                    slideContentRepository.delete(slideContent);
                    return SlideContent.toContentRespDto(slideContent);
                })
                .orElseThrow(() -> new RuntimeException("Slide Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<SlideContentDto>> searchSlideContents(String searchQuery) {
        return slideContentRepository.findByContentNameContainingIgnoreCase(searchQuery)
                .stream()
                .map(SlideContent::toContentRespDto)
                .collect(Collectors.toList());
    }
}
