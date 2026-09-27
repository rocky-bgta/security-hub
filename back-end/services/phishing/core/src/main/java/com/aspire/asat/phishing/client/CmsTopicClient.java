package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterRequestDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Calls CMS topic filter APIs for topic recommendation during campaign training setup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsTopicClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    public CmsTopicFilterResponseDto filterTopicsByProduct(String productId, CmsTopicFilterRequestDto request) {
        String url = cmsServiceUrl + "/topics/filter/by-product/" + productId;
        return callFilter(url, request);
    }

    public CmsTopicFilterResponseDto filterTopicsByPackage(String packageId, CmsTopicFilterRequestDto request) {
        String url = cmsServiceUrl + "/topics/filter/by-package/" + packageId;
        return callFilter(url, request);
    }

    public CmsTopicFilterResponseDto filterTopics(CmsTopicFilterRequestDto request) {
        String url = cmsServiceUrl + "/topics/filter";
        return callFilter(url, request);
    }

    public CmsTopicFilterResponseDto recommendTopicsByProduct(String productId, CmsTopicFilterRequestDto request) {
        String url = cmsServiceUrl + "/topics/recommend/by-product/" + productId;
        return callFilter(url, request);
    }

    public CmsTopicFilterResponseDto recommendTopicsByPackage(String packageId, CmsTopicFilterRequestDto request) {
        String url = cmsServiceUrl + "/topics/recommend/by-package/" + packageId;
        return callFilter(url, request);
    }

    public CmsTopicFilterResponseDto recommendTopics(CmsTopicFilterRequestDto request) {
        String url = cmsServiceUrl + "/topics/recommend";
        return callFilter(url, request);
    }

    private CmsTopicFilterResponseDto callFilter(String url, CmsTopicFilterRequestDto request) {
        log.info("CMS topic filter request: POST {} body={}", url, toRequestBodyJson(request));
        try {
            ApiResponseDto<CmsTopicFilterResponseDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsTopicFilterResponseDto>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                return new CmsTopicFilterResponseDto();
            }
            return response.getData();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to filter topics from CMS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter topics from CMS", e);
        }
    }

    private String toRequestBodyJson(CmsTopicFilterRequestDto request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return String.valueOf(request);
        }
    }
}
