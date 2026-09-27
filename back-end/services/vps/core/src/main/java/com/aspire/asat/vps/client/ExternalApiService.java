package com.aspire.asat.vps.client;

import com.aspire.asat.vps.dto.interactive.ContentResponse;
import com.aspire.asat.vps.dto.interactive.ContentUpdateRequest;
import com.aspire.asat.vps.dto.interactive.InteractiveVideo;
import com.aspire.asat.vps.dto.interactive.SpecificContent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ExternalApiService {
    @Value("${rest.template.base-url}")
    private String baseUrl = "http://asat-cms-service:5050/cms";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private static final ConcurrentHashMap<String, Object> contentLockMap = new ConcurrentHashMap<>();

    public ContentResponse getContentById(String id) {
        String url = baseUrl + "/api/v1/contents/" + id;
        log.debug("Fetching content from URL: {}", url);
        ContentResponse response = restTemplate.getForObject(url, ContentResponse.class);
        if (response != null) {
            return response;
        } else {
            throw new RuntimeException("Failed to fetch content with id: " + id);
        }
    }

    public void updateInteractiveVideoAttributes(String id, String contentSpecificId, String processVideoUrl, String processingStatus) {
        log.info("start updating interactive video attributes for id: {}, contentSpecificId: {}", id, contentSpecificId);
        Object lock = contentLockMap.computeIfAbsent(id, k -> new Object());

        synchronized (lock) {
            log.debug("Lock acquired for content id: {}", id);
            try {
                ContentResponse contentResponse = getContentById(id);
                if (contentResponse != null && contentResponse.getData() != null) {
                    SpecificContent specificContent = contentResponse.getData().getSpecific();
                    InteractiveVideo interactiveVideo = specificContent.getInteractiveVideo();

                    if (interactiveVideo != null && contentSpecificId.equals(interactiveVideo.getId())) {
                        if (isProcessing(interactiveVideo.getProcessingStatus())) {
                            interactiveVideo.setProcessingStatus(processingStatus);
                            interactiveVideo.setIsProcessing("false");
                            interactiveVideo.setProcessVideoUrl(processVideoUrl);
                            log.debug("Content specific id matched root interactiveVideo");
                        } else {
                            log.debug("Root interactiveVideo is not in PROCESSING state: {}", interactiveVideo.getProcessingStatus());
                        }
                    } else {
                        applyCompletionToInteractiveVideoByLanguage(
                                specificContent, contentSpecificId, processVideoUrl, processingStatus);
                        applyCompletionToNestedContentLists(
                                specificContent, contentSpecificId, processVideoUrl, processingStatus);
                    }

                    normalizeInteractiveVideoByLanguageForPut(specificContent);

                    contentResponse.getData().setSpecific(specificContent);
                    ContentUpdateRequest contentUpdateRequest = new ContentUpdateRequest();
                    contentUpdateRequest.setCommon(contentResponse.getData().getCommon());
                    contentUpdateRequest.setSpecific(contentResponse.getData().getSpecific());

                    String updatedUrl = baseUrl + "/api/v1/contents/" + id;
                    log.debug("Updating content at URL: {}", updatedUrl);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<ContentUpdateRequest> requestEntity = new HttpEntity<>(contentUpdateRequest, headers);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(updatedUrl, HttpMethod.PUT, requestEntity, String.class);

                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        log.info("Content updated successfully for id {}", id);
                    } else {
                        log.warn("Failed to update content, status: {}", responseEntity.getStatusCode());
                    }
                    TimeUnit.MILLISECONDS.sleep(1500L);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                contentLockMap.remove(id);
                log.debug("Lock released for content id: {}", id);
            }
        }
    }

    /**
     * CMS may return each language video as Map, POJO, or other JSON shape. Always coerce to a mutable map
     * so we can apply processing completion even when {@code contentList} is empty (main language video only).
     */
    private void applyCompletionToInteractiveVideoByLanguage(
            SpecificContent specificContent,
            String contentSpecificId,
            String processVideoUrl,
            String processingStatus) {
        Object byLang = specificContent.getInteractiveVideoByLanguage();
        if (byLang == null) {
            return;
        }
        if (byLang instanceof List<?> list) {
            List<Object> newList = new ArrayList<>();
            for (Object item : list) {
                Map<String, Object> videoMap = toMutableVideoMap(item);
                if (applyIfMatchingVideoMap(videoMap, contentSpecificId, processVideoUrl, processingStatus)) {
                    log.debug("Matched interactiveVideoByLanguage list entry for contentSpecificId {}", contentSpecificId);
                }
                newList.add(videoMap);
            }
            specificContent.setInteractiveVideoByLanguage(newList);
        } else if (byLang instanceof Map<?, ?> map) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                Map<String, Object> videoMap = toMutableVideoMap(e.getValue());
                if (applyIfMatchingVideoMap(videoMap, contentSpecificId, processVideoUrl, processingStatus)) {
                    log.debug("Matched interactiveVideoByLanguage map entry for contentSpecificId {}", contentSpecificId);
                }
                newMap.put(String.valueOf(e.getKey()), videoMap);
            }
            specificContent.setInteractiveVideoByLanguage(newMap);
        }
    }

    /**
     * Nested VIDEO/ANIMATION blocks inside per-language {@code contentList}. Mutates the same Map graph returned by CMS GET
     * (in-place) so the PUT payload includes completion updates.
     */
    private void applyCompletionToNestedContentLists(
            SpecificContent specificContent,
            String contentSpecificId,
            String processVideoUrl,
            String processingStatus) {
        Object byLang = specificContent.getInteractiveVideoByLanguage();
        if (byLang == null) {
            return;
        }
        List<?> videos = byLang instanceof List<?> list
                ? list
                : new ArrayList<>(((Map<?, ?>) byLang).values());
        for (Object videoItem : videos) {
            if (!(videoItem instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> langVideo = (Map<String, Object>) videoItem;
            Object contentListObj = langVideo.get("contentList");
            if (!(contentListObj instanceof List<?> contentList) || contentList.isEmpty()) {
                continue;
            }
            for (Object cli : contentList) {
                if (!(cli instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) cli;
                if (!"VIDEO".equals(item.get("contentType")) && !"ANIMATION".equals(item.get("contentType"))) {
                    continue;
                }
                Object bodyObj = item.get("contentBody");
                if (!(bodyObj instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) bodyObj;
                Object specObj = body.get("specificContent");
                if (!(specObj instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedSpec = (Map<String, Object>) specObj;
                Object nestedIv = nestedSpec.get("interactiveVideo");
                if (nestedIv instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedVideoMap = (Map<String, Object>) nestedIv;
                    applyIfMatchingVideoMap(nestedVideoMap, contentSpecificId, processVideoUrl, processingStatus);
                }
                Object nestedByLang = nestedSpec.get("interactiveVideoByLanguage");
                if (nestedByLang instanceof List<?> nList) {
                    for (Object o : nList) {
                        if (o instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> vm = (Map<String, Object>) o;
                            applyIfMatchingVideoMap(vm, contentSpecificId, processVideoUrl, processingStatus);
                        }
                    }
                } else if (nestedByLang instanceof Map<?, ?> nMap) {
                    for (Object o : nMap.values()) {
                        if (o instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> vm = (Map<String, Object>) o;
                            applyIfMatchingVideoMap(vm, contentSpecificId, processVideoUrl, processingStatus);
                        }
                    }
                }
            }
        }
    }

    private void normalizeInteractiveVideoByLanguageForPut(SpecificContent specificContent) {
        Object byLangForPut = specificContent.getInteractiveVideoByLanguage();
        if (byLangForPut instanceof Map) {
            List<Map<String, Object>> array = new ArrayList<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) byLangForPut).entrySet()) {
                if (e.getValue() instanceof Map) {
                    Map<String, Object> videoMap = new LinkedHashMap<>((Map<String, Object>) e.getValue());
                    if (!videoMap.containsKey("language") || videoMap.get("language") == null) {
                        videoMap.put("language", String.valueOf(e.getKey()));
                    }
                    array.add(videoMap);
                }
            }
            specificContent.setInteractiveVideoByLanguage(array);
        }
        Object updatedByLangForPut = specificContent.getInteractiveVideoByLanguage();
        List<?> videosForPut = updatedByLangForPut instanceof List
                ? (List<?>) updatedByLangForPut
                : (updatedByLangForPut instanceof Map ? ((Map<?, ?>) updatedByLangForPut).values().stream().toList() : List.of());
        for (Object videoItem : videosForPut) {
            if (!(videoItem instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> langVideo = (Map<String, Object>) videoItem;
            Object contentListObj = langVideo.get("contentList");
            if (!(contentListObj instanceof List<?> cl) || cl.isEmpty()) {
                continue;
            }
            for (Object cli : cl) {
                if (!(cli instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) cli;
                if (!"VIDEO".equals(item.get("contentType")) && !"ANIMATION".equals(item.get("contentType"))) {
                    continue;
                }
                Object bodyObj = item.get("contentBody");
                if (!(bodyObj instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) bodyObj;
                Object specObj = body.get("specificContent");
                if (!(specObj instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) specObj;
                Object nestedByLangForPut = nested.get("interactiveVideoByLanguage");
                if (nestedByLangForPut instanceof Map) {
                    List<Map<String, Object>> array = new ArrayList<>();
                    for (Map.Entry<?, ?> e : ((Map<?, ?>) nestedByLangForPut).entrySet()) {
                        if (e.getValue() instanceof Map) {
                            Map<String, Object> videoMap = new LinkedHashMap<>((Map<String, Object>) e.getValue());
                            videoMap.put("language", String.valueOf(e.getKey()));
                            array.add(videoMap);
                        }
                    }
                    nested.put("interactiveVideoByLanguage", array);
                }
            }
        }
    }

    private Map<String, Object> toMutableVideoMap(Object item) {
        if (item == null) {
            return new LinkedHashMap<>();
        }
        if (item instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {});
    }

    private static boolean applyIfMatchingVideoMap(
            Map<String, Object> videoMap,
            String contentSpecificId,
            String processVideoUrl,
            String processingStatus) {
        Object idObj = videoMap.get("id");
        if (idObj == null || !contentSpecificId.equals(String.valueOf(idObj))) {
            return false;
        }
        if (!isProcessing(videoMap.get("processingStatus"))) {
            return false;
        }
        videoMap.put("processingStatus", processingStatus);
        videoMap.put("isProcessing", "false");
        videoMap.put("processVideoUrl", processVideoUrl);
        return true;
    }

    private static boolean isProcessing(Object status) {
        if (status == null) {
            return false;
        }
        return "PROCESSING".equalsIgnoreCase(String.valueOf(status).trim());
    }
}
