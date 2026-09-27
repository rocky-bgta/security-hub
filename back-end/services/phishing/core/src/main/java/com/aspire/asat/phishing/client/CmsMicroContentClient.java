package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.cms.CmsChapterCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsChapterRespDto;
import com.aspire.asat.phishing.dto.cms.CmsContentCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsContentRespDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsTopicRespDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Calls CMS Topic/Chapter/Content creation APIs (base URL from {@code service.cms.url}, e.g.
 * {@code .../cms/api/v1}) to build the "microContent" training material for a client.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsMicroContentClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final UserCurrentContextService userCurrentContextService;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    /**
     * Creates a topic in CMS and returns its id.
     */
    public String createTopic(CmsTopicCreateRequest request) {
        String url = cmsServiceUrl + "/topics";
        log.info("Creating CMS topic: name={}", request.getTopicName());
        try {
            ApiResponseDto<CmsTopicRespDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsTopicRespDto>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            String id = extractId(response == null ? null : response.getData() == null ? null : response.getData().getId(),
                    "topic");
            log.info("CMS topic created: id={}", id);
            return id;
        } catch (WebClientResponseException e) {
            throw fail("topic", e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to create CMS topic", HttpStatus.BAD_GATEWAY, e);
        }
    }

    /**
     * Deletes (soft-disables) a CMS topic by id. Forwards the inbound {@code CurrentContext}
     * header so CMS can authorize the caller.
     */
    public void deleteTopic(String topicId) {
        String url = cmsServiceUrl + "/topics/" + topicId;
        log.info("Deleting CMS topic: id={}", topicId);
        try {
            webClient.delete()
                    .uri(url)
                    .header(UserCurrentContextService.HEADER_CURRENT_USER_CONTEXT,
                            userCurrentContextService.getCurrentUserContextHeaderValue())
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            log.info("CMS topic deleted: id={}", topicId);
        } catch (WebClientResponseException e) {
            throw failDelete(e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to delete CMS topic", HttpStatus.BAD_GATEWAY, e);
        }
    }

    /**
     * Creates a chapter in CMS and returns its id.
     */
    public String createChapter(CmsChapterCreateRequest request) {
        String url = cmsServiceUrl + "/chapters";
        log.info("Creating CMS chapter: topicId={}, name={}", request.getTopicId(), request.getChapterName());
        try {
            ApiResponseDto<CmsChapterRespDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsChapterRespDto>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            String id = extractId(response == null ? null : response.getData() == null ? null : response.getData().getId(),
                    "chapter");
            log.info("CMS chapter created: id={}", id);
            return id;
        } catch (WebClientResponseException e) {
            throw fail("chapter", e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to create CMS chapter", HttpStatus.BAD_GATEWAY, e);
        }
    }

    /**
     * Creates a content in CMS and returns its id.
     */
    public String createContent(CmsContentCreateRequest request) {
        String url = cmsServiceUrl + "/contents";
        log.info("Creating CMS content: name={}",
                request.getCommon() == null ? null : request.getCommon().getContentName());
        try {
            ApiResponseDto<CmsContentRespDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsContentRespDto>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            String id = extractId(response == null ? null : response.getData() == null ? null : response.getData().getId(),
                    "content");
            log.info("CMS content created: id={}", id);
            return id;
        } catch (WebClientResponseException e) {
            throw fail("content", e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to create CMS content", HttpStatus.BAD_GATEWAY, e);
        }
    }

    private String extractId(String id, String entity) {
        if (id == null || id.isBlank()) {
            throw new ServiceException("CMS " + entity + " creation returned no id", HttpStatus.BAD_GATEWAY);
        }
        return id;
    }

    private ServiceException fail(String entity, WebClientResponseException e) {
        log.error("CMS {} create failed: status={}, body={}", entity, e.getStatusCode(), e.getResponseBodyAsString());
        return new ServiceException("Failed to create CMS " + entity + ": " + e.getResponseBodyAsString(),
                HttpStatus.BAD_GATEWAY, e);
    }

    private ServiceException failDelete(WebClientResponseException e) {
        log.error("CMS topic delete failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        return new ServiceException("Failed to delete CMS topic: " + e.getResponseBodyAsString(),
                HttpStatus.BAD_GATEWAY, e);
    }
}
