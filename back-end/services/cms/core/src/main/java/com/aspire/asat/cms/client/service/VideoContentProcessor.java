package com.aspire.asat.cms.client.service;

import com.aspire.asat.cms.client.dto.VideoProcessRequestDto;
import com.aspire.asat.cms.client.dto.VideoProcessResponseDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.interactive.ContentListItem;
import com.aspire.asat.cms.dto.interactive.ContentUpdateRequest;
import com.aspire.asat.cms.dto.interactive.InteractiveVideo;
import com.aspire.asat.cms.dto.interactive.SpecificContent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class VideoContentProcessor {

    private final RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    @Value("${web.client.base.url}")
    private String baseUrl;


    public VideoContentProcessor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ContentRespDto<?> processInteractiveVideoContent(String id, ContentReqDto<?> contentReqDto) {


        log.info("start Processing interactive video content for id: " + id);


        ContentUpdateRequest contentUpdateRequest = new ContentUpdateRequest();
        contentUpdateRequest.setCommon(contentReqDto.getCommon());
        Object specificContent = contentReqDto.getSpecific();

        SpecificContent specific = objectMapper.convertValue(
                specificContent,
                new TypeReference<SpecificContent>() {}
        );

        contentUpdateRequest.setSpecific(specific);

        InteractiveVideo interactiveVideo = contentUpdateRequest.getSpecific().getInteractiveVideo();
        log.info("Interactive Video 1stt: " + interactiveVideo);
        if(interactiveVideo != null
                && "QUEUE".equals(interactiveVideo.getProcessingStatus())
                && isTruthyProcessingFlag(interactiveVideo.getIsProcessing())
                && interactiveVideo.getVideoUrl() != null)
        {
            VideoProcessRequestDto videoProcessRequest = getVideoProcessRequestDto(id, interactiveVideo);
            callVideoProcessingService(videoProcessRequest);
            interactiveVideo.setProcessingStatus("PROCESSING");
            contentUpdateRequest.getSpecific().setInteractiveVideo(interactiveVideo);
        }

        // multi-language: process interactiveVideoByLanguage (array)
        if (contentUpdateRequest.getSpecific().getInteractiveVideoByLanguage() != null
                && contentUpdateRequest.getSpecific().getInteractiveVideoByLanguage() instanceof List) {
            List<?> list = (List<?>) contentUpdateRequest.getSpecific().getInteractiveVideoByLanguage();
            for (Object item : list) {
                if (!(item instanceof Map)) continue;
                Map<String, Object> map = (Map<String, Object>) item;
                InteractiveVideo video = objectMapper.convertValue(map, InteractiveVideo.class);
                if (video != null
                        && "QUEUE".equals(video.getProcessingStatus())
                        && isTruthyProcessingFlag(video.getIsProcessing())
                        && video.getVideoUrl() != null) {
                    if (video.getId() == null || video.getId().isEmpty()) {
                        video.setId(UUID.randomUUID().toString());
                        map.put("id", video.getId());
                    }
                    VideoProcessRequestDto videoProcessRequest = getVideoProcessRequestDto(id, video);
                    callVideoProcessingService(videoProcessRequest);
                    map.put("processingStatus", "PROCESSING");
                    // Keep true while VPS is processing; VPS callback sets false on completion.
                    map.put("isProcessing", "true");
                }
            }
        }

        if (contentUpdateRequest.getSpecific().getInteractiveVideoByLanguage() instanceof List<?> byLanguageList) {
            for (Object byLanguageItem : byLanguageList) {
                InteractiveVideo languageVideo = objectMapper.convertValue(byLanguageItem, InteractiveVideo.class);
                if (languageVideo == null || languageVideo.getContentList() == null || languageVideo.getContentList().isEmpty()) {
                    continue;
                }
                for (ContentListItem contentListItem : languageVideo.getContentList()) {
                    if (contentListItem == null) continue;
                    if (!"VIDEO".equals(contentListItem.getContentType())
                            && !"ANIMATION".equals(contentListItem.getContentType())) {
                        continue;
                    }
                    if (contentListItem.getContentBody() == null || contentListItem.getContentBody().getSpecificContent() == null) continue;
                    InteractiveVideo interactiveVideo1 = contentListItem.getContentBody().getSpecificContent().getInteractiveVideo();
                    if (interactiveVideo1 != null
                            && "QUEUE".equals(interactiveVideo1.getProcessingStatus())
                            && isTruthyProcessingFlag(interactiveVideo1.getIsProcessing())
                            && interactiveVideo1.getVideoUrl() != null) {
                        VideoProcessRequestDto videoProcessRequest = getVideoProcessRequestDto(id, interactiveVideo1);
                        callVideoProcessingService(videoProcessRequest);
                        interactiveVideo1.setProcessingStatus("PROCESSING");
                        contentListItem.getContentBody().getSpecificContent().setInteractiveVideo(interactiveVideo1);
                    }
                    if (contentListItem.getContentBody().getSpecificContent().getInteractiveVideoByLanguage() instanceof List<?> nestedList) {
                        for (Object item : nestedList) {
                            if (!(item instanceof Map)) continue;
                            Map<String, Object> map = (Map<String, Object>) item;
                            InteractiveVideo video = objectMapper.convertValue(map, InteractiveVideo.class);
                            if (video != null
                                    && "QUEUE".equals(video.getProcessingStatus())
                                    && isTruthyProcessingFlag(video.getIsProcessing())
                                    && video.getVideoUrl() != null) {
                                if (video.getId() == null || video.getId().isEmpty()) {
                                    video.setId(UUID.randomUUID().toString());
                                    map.put("id", video.getId());
                                }
                                VideoProcessRequestDto videoProcessRequest = getVideoProcessRequestDto(id, video);
                                callVideoProcessingService(videoProcessRequest);
                                map.put("processingStatus", "PROCESSING");
                                map.put("isProcessing", "true");
                            }
                        }
                    }
                }
            }
        } else if (contentUpdateRequest.getSpecific().getContentList() != null) {
            // Keep existing behavior for non-language interactive content.
            List<ContentListItem> contentList = contentUpdateRequest.getSpecific().getContentList();
            if (!contentList.isEmpty()) {
                for (ContentListItem contentListItem : contentList) {
                    if (!"VIDEO".equals(contentListItem.getContentType())
                            && !"ANIMATION".equals(contentListItem.getContentType())) {
                        continue;
                    }
                    if (contentListItem.getContentBody() == null || contentListItem.getContentBody().getSpecificContent() == null) continue;
                    InteractiveVideo interactiveVideo1 = contentListItem.getContentBody().getSpecificContent().getInteractiveVideo();
                    if (interactiveVideo1 != null
                            && "QUEUE".equals(interactiveVideo1.getProcessingStatus())
                            && isTruthyProcessingFlag(interactiveVideo1.getIsProcessing())
                            && interactiveVideo1.getVideoUrl() != null) {
                        VideoProcessRequestDto videoProcessRequest = getVideoProcessRequestDto(id, interactiveVideo1);
                        callVideoProcessingService(videoProcessRequest);
                        interactiveVideo1.setProcessingStatus("PROCESSING");
                        contentListItem.getContentBody().getSpecificContent().setInteractiveVideo(interactiveVideo1);
                    }
                    if (contentListItem.getContentBody().getSpecificContent().getInteractiveVideoByLanguage() instanceof List<?> nestedList) {
                        for (Object item : nestedList) {
                            if (!(item instanceof Map)) continue;
                            Map<String, Object> map = (Map<String, Object>) item;
                            InteractiveVideo video = objectMapper.convertValue(map, InteractiveVideo.class);
                            if (video != null
                                    && "QUEUE".equals(video.getProcessingStatus())
                                    && isTruthyProcessingFlag(video.getIsProcessing())
                                    && video.getVideoUrl() != null) {
                                if (video.getId() == null || video.getId().isEmpty()) {
                                    video.setId(UUID.randomUUID().toString());
                                    map.put("id", video.getId());
                                }
                                VideoProcessRequestDto videoProcessRequest = getVideoProcessRequestDto(id, video);
                                callVideoProcessingService(videoProcessRequest);
                                map.put("processingStatus", "PROCESSING");
                                map.put("isProcessing", "true");
                            }
                        }
                    }
                }
                contentUpdateRequest.getSpecific().setContentList(contentUpdateRequest.getSpecific().getContentList());
            }
        }
        contentUpdateRequest.setSpecific(contentUpdateRequest.getSpecific());

        Instant now = Instant.now();
        return ContentRespDto
                .builder()
                .id(id.toString())
                .common(contentUpdateRequest.getCommon())
                .specific(contentUpdateRequest.getSpecific())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static VideoProcessRequestDto getVideoProcessRequestDto(String id, InteractiveVideo interactiveVideo) {
        VideoProcessRequestDto videoProcessRequest = new VideoProcessRequestDto();
        videoProcessRequest.setId(id);
        videoProcessRequest.setSpecificContentId(interactiveVideo.getId());
        videoProcessRequest.setContentType("INTERACTIVE_VIDEO");
        videoProcessRequest.setProcessingStatus("PROCESSING");
        videoProcessRequest.setVideoUrl(interactiveVideo.getVideoUrl());
        videoProcessRequest.setProcessedVideoUrl(interactiveVideo.getProcessVideoUrl());
        videoProcessRequest.setUserId("Aspire");
        return videoProcessRequest;
    }

    private void callVideoProcessingService(VideoProcessRequestDto requestDto) {
        String url = baseUrl + "/api/v1/video-process";
        log.info("Processing video content at URL: " + url);
        log.info("request send to vps for id: " + requestDto.getId()+" and specific content id: " + requestDto.getSpecificContentId()+ " with time:"+ Instant.now());
        VideoProcessResponseDto response = restTemplate.postForObject(url, requestDto, VideoProcessResponseDto.class);
        if (response != null) {
            log.info("Video processing response: " + response);
        } else {
            throw new RuntimeException("Failed to process video content with id: " + requestDto.getId());
        }
    }

    /** JSON may send boolean {@code true} or string {@code "true"}; tolerate both after Jackson binding to String. */
    private static boolean isTruthyProcessingFlag(String isProcessing) {
        if (isProcessing == null || isProcessing.isBlank()) {
            return false;
        }
        return "true".equalsIgnoreCase(isProcessing.trim());
    }
}
