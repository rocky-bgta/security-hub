package com.aspire.asat.cms.service.impl;


import com.aspire.asat.cms.client.service.VideoContentProcessor;
import com.aspire.asat.cms.dto.chapter.ChapterResponseWithItemDto;
import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.dto.interactive.ContentListItem;
import com.aspire.asat.cms.dto.interactive.InteractiveVideo;
import com.aspire.asat.cms.dto.interactive.SpecificContent;
import com.aspire.asat.cms.dto.interactive.SpecificContentDto;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Content;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ContentRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.ChapterService;
import com.aspire.asat.cms.service.ContentService;
import com.aspire.asat.cms.util.ContentSpecificResponseNormalizer;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final ChapterService chapterService;
    private final VideoContentProcessor videoContentProcessor;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    public ContentServiceImpl(ContentRepository contentRepository, TopicRepository topicRepository, ChapterService chapterService, VideoContentProcessor videoContentProcessor, FileService fileService, ObjectMapper objectMapper) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.chapterService = chapterService;
        this.videoContentProcessor = videoContentProcessor;
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    // multi-language: convert interactiveVideoByLanguage array to map before save
    private void mergeInteractiveVideoByLanguage(SpecificContent specific) {
        if (specific.getInteractiveVideoByLanguage() != null && specific.getInteractiveVideoByLanguage() instanceof List) {
            Map<String, InteractiveVideo> map = buildMapFromVideoList((List<?>) specific.getInteractiveVideoByLanguage());
            specific.setInteractiveVideoByLanguage(map);
        }
        Object byLanguage = specific.getInteractiveVideoByLanguage();
        if (byLanguage instanceof Map<?, ?> videoMap) {
            for (Object value : videoMap.values()) {
                InteractiveVideo video = objectMapper.convertValue(value, InteractiveVideo.class);
                if (video == null || video.getContentList() == null) {
                    continue;
                }
                for (ContentListItem item : video.getContentList()) {
                    if (item == null) {
                        continue;
                    }
                    if (("VIDEO".equals(item.getContentType()) || "ANIMATION".equals(item.getContentType()))
                            && item.getContentBody() != null && item.getContentBody().getSpecificContent() != null) {
                        SpecificContentDto nested = item.getContentBody().getSpecificContent();
                        if (nested.getInteractiveVideoByLanguage() != null && nested.getInteractiveVideoByLanguage() instanceof List) {
                            Map<String, InteractiveVideo> nestedMap = buildMapFromVideoList((List<?>) nested.getInteractiveVideoByLanguage());
                            nested.setInteractiveVideoByLanguage(nestedMap);
                        }
                    }
                }
            }
        } else if (specific.getContentList() != null) {
            // Keep nested VIDEO/ANIMATION conversion for non-language interactive flows.
            for (ContentListItem item : specific.getContentList()) {
                if (("VIDEO".equals(item.getContentType()) || "ANIMATION".equals(item.getContentType()))
                        && item.getContentBody() != null && item.getContentBody().getSpecificContent() != null) {
                    SpecificContentDto nested = item.getContentBody().getSpecificContent();
                    if (nested.getInteractiveVideoByLanguage() != null && nested.getInteractiveVideoByLanguage() instanceof List) {
                        Map<String, InteractiveVideo> nestedMap = buildMapFromVideoList((List<?>) nested.getInteractiveVideoByLanguage());
                        nested.setInteractiveVideoByLanguage(nestedMap);
                    }
                }
            }
        }
    }

    private Map<String, InteractiveVideo> buildMapFromVideoList(List<?> list) {
        Map<String, InteractiveVideo> map = new LinkedHashMap<>();
        if (list == null) return map;
        for (Object item : list) {
            InteractiveVideo video = objectMapper.convertValue(item, InteractiveVideo.class);
            if (video == null) continue;
            String lang = (video.getLanguage() != null && !video.getLanguage().isBlank()) ? video.getLanguage().trim() : "en";
            if (video.getId() == null || video.getId().isEmpty()) {
                video.setId(UUID.randomUUID().toString());
            }
            map.put(lang, video);
        }
        return map;
    }

    /**
     * After VPS marks a language row PROCESSED, the next save often still sends stale PROCESSING from the UI.
     * If the video file ({@code videoUrl}) and row {@code id} match what is already PROCESSED in DB, keep processed fields.
     * Does not apply when the client sends QUEUE (new encode request).
     */
    private void preserveProcessedVideoStateOnUpdate(SpecificContent incoming, Object existingSpecific) {
        if (incoming == null || existingSpecific == null) {
            return;
        }
        Map<String, ExistingProcessedVideo> existingById = indexProcessedLanguageVideosById(existingSpecific);
        if (existingById.isEmpty()) {
            return;
        }
        Object byLang = incoming.getInteractiveVideoByLanguage();
        if (byLang instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> e : rawMap.entrySet()) {
                InteractiveVideo v = objectMapper.convertValue(e.getValue(), InteractiveVideo.class);
                if (v == null || v.getId() == null) {
                    continue;
                }
                if (isQueueStatus(v.getProcessingStatus())) {
                    continue;
                }
                ExistingProcessedVideo ex = existingById.get(v.getId());
                if (ex == null || !ex.processed) {
                    continue;
                }
                if (!Objects.equals(ex.videoUrl, v.getVideoUrl())) {
                    continue;
                }
                if (isProcessingStatus(v.getProcessingStatus())) {
                    v.setProcessingStatus(ex.processingStatus);
                    v.setIsProcessing(ex.isProcessing);
                    if (ex.processVideoUrl != null) {
                        v.setProcessVideoUrl(ex.processVideoUrl);
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, InteractiveVideo> typed = (Map<String, InteractiveVideo>) incoming.getInteractiveVideoByLanguage();
                    typed.put(String.valueOf(e.getKey()), v);
                }
            }
        } else if (byLang instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                InteractiveVideo v = objectMapper.convertValue(list.get(i), InteractiveVideo.class);
                if (v == null || v.getId() == null) {
                    continue;
                }
                if (isQueueStatus(v.getProcessingStatus())) {
                    continue;
                }
                ExistingProcessedVideo ex = existingById.get(v.getId());
                if (ex == null || !ex.processed) {
                    continue;
                }
                if (!Objects.equals(ex.videoUrl, v.getVideoUrl())) {
                    continue;
                }
                if (isProcessingStatus(v.getProcessingStatus())) {
                    v.setProcessingStatus(ex.processingStatus);
                    v.setIsProcessing(ex.isProcessing);
                    if (ex.processVideoUrl != null) {
                        v.setProcessVideoUrl(ex.processVideoUrl);
                    }
                    @SuppressWarnings("unchecked")
                    List<Object> mutable = (List<Object>) list;
                    mutable.set(i, v);
                }
            }
        }
    }

    private Map<String, ExistingProcessedVideo> indexProcessedLanguageVideosById(Object existingSpecific) {
        Map<String, ExistingProcessedVideo> out = new HashMap<>();
        SpecificContent ex = objectMapper.convertValue(existingSpecific, SpecificContent.class);
        if (ex == null) {
            return out;
        }
        addProcessedSnapshots(ex.getInteractiveVideoByLanguage(), out);
        return out;
    }

    private void addProcessedSnapshots(Object byLang, Map<String, ExistingProcessedVideo> out) {
        if (byLang == null) {
            return;
        }
        if (byLang instanceof Map<?, ?> m) {
            for (Object val : m.values()) {
                putIfProcessed(val, out);
            }
        } else if (byLang instanceof List<?> list) {
            for (Object val : list) {
                putIfProcessed(val, out);
            }
        }
    }

    private void putIfProcessed(Object val, Map<String, ExistingProcessedVideo> out) {
        InteractiveVideo iv = objectMapper.convertValue(val, InteractiveVideo.class);
        if (iv == null || iv.getId() == null) {
            return;
        }
        if (!isProcessedStatus(iv.getProcessingStatus())) {
            return;
        }
        out.put(iv.getId(), new ExistingProcessedVideo(
                true,
                iv.getProcessingStatus(),
                iv.getIsProcessing(),
                iv.getProcessVideoUrl(),
                iv.getVideoUrl()
        ));
    }

    private static boolean isProcessedStatus(String status) {
        return status != null && "PROCESSED".equalsIgnoreCase(status.trim());
    }

    private static boolean isProcessingStatus(String status) {
        return status != null && "PROCESSING".equalsIgnoreCase(status.trim());
    }

    private static boolean isQueueStatus(String status) {
        return status != null && "QUEUE".equalsIgnoreCase(status.trim());
    }

    private static final class ExistingProcessedVideo {
        final boolean processed;
        final String processingStatus;
        final String isProcessing;
        final String processVideoUrl;
        final String videoUrl;

        ExistingProcessedVideo(boolean processed, String processingStatus, String isProcessing, String processVideoUrl, String videoUrl) {
            this.processed = processed;
            this.processingStatus = processingStatus;
            this.isProcessing = isProcessing;
            this.processVideoUrl = processVideoUrl;
            this.videoUrl = videoUrl;
        }
    }

    @Override
    public List<ContentRespDto<?>> getAllContents(String search, com.aspire.asat.cms.dto.enums.CommonStatus status, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<Content> pageContent;

        if (search != null && !search.isEmpty() && status != null) {
            pageContent = contentRepository.findByContentNameContainingIgnoreCaseAndStatus(search, status, pageable);
        } else if (search != null && !search.isEmpty()) {
            pageContent = contentRepository.findByContentNameContainingIgnoreCase(search, pageable);
        } else if (status != null) {
            pageContent = contentRepository.findByStatus(status, pageable);
        } else {
            pageContent = contentRepository.findAll(pageable);
        }
        return pageContent
                .getContent()
                .stream()
                .map(this::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentRespDto<?> getContentById(String contentId) {
        return contentRepository.findById(contentId)
                .map(this::toContentRespDto)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

    }

    @Override
    public long getTotalContentCount() {
        return contentRepository.count();
    }

    @Override
    public long getTotalContentCount(String search, com.aspire.asat.cms.dto.enums.CommonStatus status) {
        if (search != null && !search.isEmpty() && status != null) {
            return contentRepository.countByContentNameContainingIgnoreCaseAndStatus(search, status);
        } else if (search != null && !search.isEmpty()) {
            return contentRepository.countByContentNameContainingIgnoreCase(search);
        } else if (status != null) {
            return contentRepository.countByStatus(status);
        } else {
            return contentRepository.count();
        }
    }

    @Override
    public ContentRespDto<?> deleteContentById(String contentId) {
        return contentRepository.findById(contentId)
                .map(content -> {
                    // Get chapter IDs related to this content
                    List<String> chapterIds = content.getChapterIds();

                    if (chapterIds != null && !chapterIds.isEmpty()) {
                        for (String chapterId : chapterIds) {
                            // Find chapter by id
                            ChapterResponseWithItemDto chapter = chapterService.getChapterById(chapterId);


                            String topicId = chapter.getTopicId();

                            // Find topic by id
                            Topic topic = topicRepository.findById(topicId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

                            // Decrement totalContentCount safely
                            Integer currentCount = topic.getTotalContentCount();
                            if (currentCount == null) {
                                currentCount = 0;
                            }
                            if (currentCount > 0) {
                                topic.setTotalContentCount(currentCount - 1);
                                topic.setUpdatedAt(Instant.now());
                                topicRepository.save(topic);
                            }
                        }
                    }

                    // Delete the content after updating courses
                    contentRepository.delete(content);

                    return toContentRespDto(content);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));
    }

    @Override
    public ContentRespDto<?> createContent(ContentReqDto<?> contentReqDto) {
        UUID id2 = UUID.randomUUID();
        String id = id2.toString();
        Instant now = Instant.now();
        ContentCommonDto common = contentReqDto.getCommon();
        if (contentRepository.existsByContentName(common.getContentName())) {
            throw new DuplicateNameException("Content already exists with name: " + common.getContentName());
        }

        //Newly Added
        List<String> contentIds = List.of(id);
        List<String> chapterIds = contentReqDto.getCommon().getChapterIds();
        if (chapterIds != null && !chapterIds.isEmpty()) {
            for (String chapterId : chapterIds) {
                chapterService.updateChapterContentIds(chapterId, contentIds);

                // Find chapter details to get topicId
                ChapterResponseWithItemDto chapter = chapterService.getChapterById(chapterId);
                String topicId = chapter.getTopicId();

                // Increment totalContentCount of the topic
                Topic topic = topicRepository.findById(topicId)
                        .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

                Integer currentCount = topic.getTotalContentCount();
                if (currentCount == null) {
                    currentCount = 0;
                }
                topic.setTotalContentCount(currentCount + 1);
                topic.setUpdatedAt(Instant.now());

                topicRepository.save(topic);
            }
        }
        //save content

        Content updatedContent;

        //Interactive content
        ContentRespDto<?> responseDto;
        if (contentReqDto.getSpecific() != null &&
                (contentReqDto.getCommon().getContentType().equals(ContentType.INTERACTIVE_VIDEO) ||
                        contentReqDto.getCommon().getContentType().equals(ContentType.VIDEO) ||
                        contentReqDto.getCommon().getContentType().equals(ContentType.ANIMATION) ||
                        contentReqDto.getCommon().getContentType().equals(ContentType.INTERACTIVE_CONTENT))) {
            responseDto = videoContentProcessor.processInteractiveVideoContent(id, contentReqDto);
            SpecificContent specific = objectMapper.convertValue(responseDto.getSpecific(), SpecificContent.class);
            mergeInteractiveVideoByLanguage(specific);
            updatedContent = Content.toUpdateContent(id, responseDto.getCommon(), specific);

        }
        else{
            updatedContent = Content.toContent(id, common, now, now, contentReqDto.getSpecific());
        }
        return toContentRespDto(contentRepository.save(updatedContent));
    }

    @Override
    public ContentRespDto<?> updateContent(String contentId, ContentReqDto<?> contentReqDto) {
        return contentRepository.findById(contentId)
                .map(existingContent -> {
                    ContentCommonDto common = contentReqDto.getCommon();
                    if (!existingContent.getContentName().equals(common.getContentName())) {
                        boolean isContentNameExist = contentRepository.existsByContentName(common.getContentName());
                        if (isContentNameExist) {
                            throw new DuplicateNameException("Content already exists with name: " + common.getContentName());
                        }
                    }

                    Content updatedContent;

                    ContentRespDto<?> responseDto = new ContentRespDto<>();
                    if (contentReqDto.getSpecific() != null &&
                            (contentReqDto.getCommon().getContentType().equals(ContentType.INTERACTIVE_VIDEO) ||
                                    contentReqDto.getCommon().getContentType().equals(ContentType.VIDEO) ||
                                    contentReqDto.getCommon().getContentType().equals(ContentType.ANIMATION) ||
                                    contentReqDto.getCommon().getContentType().equals(ContentType.INTERACTIVE_CONTENT))) {

                        responseDto = videoContentProcessor.processInteractiveVideoContent(contentId, contentReqDto);
                        SpecificContent specific = objectMapper.convertValue(responseDto.getSpecific(), SpecificContent.class);
                        mergeInteractiveVideoByLanguage(specific);
                        preserveProcessedVideoStateOnUpdate(specific, existingContent.getSpecificContent());
                        updatedContent = Content.toUpdateContent(contentId, responseDto.getCommon(), specific);
                        updatedContent.setCreatedAt(existingContent.getCreatedAt());
                    }
                    else {
                        updatedContent = Content.toContent(contentId, common, Instant.now(), Instant.now(), contentReqDto.getSpecific());
                    }

                    return toContentRespDto(contentRepository.save(updatedContent));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));
    }

    public  ContentRespDto<?> toContentRespDto(Content content) {

        Object specificContent = content.getSpecificContent();
        Object specificForResponse = ContentSpecificResponseNormalizer.normalizeForJsonResponse(objectMapper, specificContent);
        return ContentRespDto.builder()
                .id(content.getId())
                .common(ContentCommonDto.builder()
                        .contentName(content.getContentName())
                        .description(content.getDescription())
                        .contentType(content.getContentType())
                        .status(content.getStatus())
                        .author(content.getAuthor())
                        .chapterIds(content.getChapterIds())
                        .tags(content.getTags())
                        .build())
                .specific(specificForResponse)
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }

}
