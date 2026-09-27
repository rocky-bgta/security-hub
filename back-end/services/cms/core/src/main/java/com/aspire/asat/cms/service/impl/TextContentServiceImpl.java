package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.TextContentDto;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.TextContent;
import com.aspire.asat.cms.repository.TextContentRepository;
import com.aspire.asat.cms.service.TextContentService;
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
public class TextContentServiceImpl implements TextContentService {

    private final TextContentRepository textContentRepository;

    @Autowired
    public TextContentServiceImpl(TextContentRepository textContentRepository) {
        this.textContentRepository = textContentRepository;
    }

    @Override
    public ContentRespDto<TextContentDto> createTextContent(ContentReqDto<TextContentDto> textContentDto) {

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        if(textContentRepository.existsByContentName(textContentDto.getCommon().getContentName())){
            throw new DuplicateNameException("Text Content already exists with name: " + textContentDto.getCommon().getContentName());
        }

        TextContent textContent = TextContent.toTextContent(id.toString(), textContentDto, now, now);
        return TextContent.toContentRespDto(textContentRepository.save(textContent));
    }

    @Override
    public ContentRespDto<TextContentDto> updateTextContent(UUID contentId, ContentReqDto<TextContentDto> textContentDto) {
        return textContentRepository.findById(contentId)
                .map(existingTextContent -> {
                    if(!existingTextContent.getContentName().equals(textContentDto.getCommon().getContentName())){
                        boolean isContentNameExist = textContentRepository.existsByContentName(textContentDto.getCommon().getContentName());
                        if(isContentNameExist){
                            throw new DuplicateNameException("Text Content already exists with name: " + textContentDto.getCommon().getContentName());
                        }
                    }
                    TextContent updatedTextContent = TextContent.toUpdateTextContent(existingTextContent, textContentDto, Instant.now());
                    return TextContent.toContentRespDto(textContentRepository.save(updatedTextContent));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Text Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<TextContentDto>> getAllTextContents(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<TextContent> pageTextContent;// = textContentRepository.findByContentType(ContentType.TEXT, pageable);

        if(search != null && !search.isEmpty()){
            pageTextContent = textContentRepository.findByContentNameContainingIgnoreCase(search, pageable);
        } else {
            pageTextContent = textContentRepository.findByContentType(ContentType.TEXT, pageable);
        }

        return pageTextContent.getContent()
                .stream()
                .map(TextContent::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentRespDto<TextContentDto> getTextContentById(UUID contentId) {
        return textContentRepository.findById(contentId)
                .map(TextContent::toContentRespDto)
                .orElseThrow(() -> new ResourceNotFoundException("Text Content not found with id: " + contentId));
    }

    @Override
    public ContentRespDto<TextContentDto> deleteTextContentById(UUID contentId) {
        return textContentRepository.findById(contentId)
                .map(textContent -> {
                    textContentRepository.delete(textContent);
                    return TextContent.toContentRespDto(textContent);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Text Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<TextContentDto>> searchTextContents(String searchQuery) {
        return textContentRepository.findByContentNameContainingIgnoreCase(searchQuery, null)
                .stream()
                .map(TextContent::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalTextContentCount() {
        return textContentRepository.countByContentType(ContentType.TEXT);
    }
}
