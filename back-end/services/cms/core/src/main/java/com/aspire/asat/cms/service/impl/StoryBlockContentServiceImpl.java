package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.storyblock.StoryBlockContentDto;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.model.StoryBlockContent;
import com.aspire.asat.cms.repository.StoryBlockContentRepository;
import com.aspire.asat.cms.service.StoryBlockContentService;
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
public class StoryBlockContentServiceImpl implements StoryBlockContentService {

    private final StoryBlockContentRepository storyBlockContentRepository;

    @Autowired
    public StoryBlockContentServiceImpl(StoryBlockContentRepository storyBlockContentRepository) {
        this.storyBlockContentRepository = storyBlockContentRepository;
    }

    @Override
    public ContentRespDto<StoryBlockContentDto> createStoryBlockContent(ContentReqDto<StoryBlockContentDto> storyBlockContentDto) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        if(storyBlockContentRepository.existsByContentName(storyBlockContentDto.getCommon().getContentName())){
            throw new RuntimeException("Story Block Content already exists with name: " + storyBlockContentDto.getCommon().getContentName());
        }
        StoryBlockContent storyBlockContent = StoryBlockContent.toStoryBlockContent(id.toString(), storyBlockContentDto, now, now);
        return StoryBlockContent.toContentRespDto(storyBlockContentRepository.save(storyBlockContent));

    }

    @Override
    public ContentRespDto<StoryBlockContentDto> updateStoryBlockContent(UUID contentId, ContentReqDto<StoryBlockContentDto> storyBlockContentDto) {
        return storyBlockContentRepository.findById(contentId)
                .map(existingStoryBlockContent -> {
                    if(!existingStoryBlockContent.getContentName().equals(storyBlockContentDto.getCommon().getContentName())){
                        boolean isContentNameExist = storyBlockContentRepository.existsByContentName(storyBlockContentDto.getCommon().getContentName());
                        if(isContentNameExist){
                            throw new RuntimeException("Story Block Content already exists with name: " + storyBlockContentDto.getCommon().getContentName());
                        }
                    }
                    StoryBlockContent updatedStoryBlockContent = StoryBlockContent.toUpdateStoryBlockContent(existingStoryBlockContent, storyBlockContentDto, Instant.now());
                    return StoryBlockContent.toContentRespDto(storyBlockContentRepository.save(updatedStoryBlockContent));
                })
                .orElseThrow(() -> new RuntimeException("Story Block Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<StoryBlockContentDto>> getAllStoryBlockContents(Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<StoryBlockContent> storyBlockContents = storyBlockContentRepository.findByContentType(ContentType.STORY_BLOCK, pageable);

        return storyBlockContents.stream()
                .map(StoryBlockContent::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentRespDto<StoryBlockContentDto> getStoryBlockContentById(UUID contentId) {
        return storyBlockContentRepository.findById(contentId)
                .map(StoryBlockContent::toContentRespDto)
                .orElseThrow(() -> new RuntimeException("Story Block Content not found with id: " + contentId));
    }

    @Override
    public ContentRespDto<StoryBlockContentDto> deleteStoryBlockContentById(UUID contentId) {
        return storyBlockContentRepository.findById(contentId)
                .map(storyBlockContent -> {
                    storyBlockContentRepository.delete(storyBlockContent);
                    return StoryBlockContent.toContentRespDto(storyBlockContent);
                })
                .orElseThrow(() -> new RuntimeException("Story Block Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<StoryBlockContentDto>> searchStoryBlockContents(String searchQuery) {
        return storyBlockContentRepository.findByContentNameContainingIgnoreCase(searchQuery)
                .stream()
                .map(StoryBlockContent::toContentRespDto)
                .collect(Collectors.toList());
    }
}
