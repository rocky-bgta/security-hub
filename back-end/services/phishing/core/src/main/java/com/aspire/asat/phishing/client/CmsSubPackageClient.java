package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.cms.CmsSubPackageCreateRequest;
import com.aspire.asat.phishing.dto.cms.CmsSubPackageCreatedData;
import com.aspire.asat.phishing.dto.cms.CmsSubPackageData;
import com.aspire.asat.phishing.dto.cms.CmsSubPackageUpdateRequest;
import com.aspire.asat.phishing.dto.cms.CmsTopicDetailData;
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
import java.util.Optional;

/**
 * Calls CMS sub-package and topic APIs (base URL from {@code service.cms.url}, e.g. {@code .../cms/api/v1}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsSubPackageClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    public String createSubPackage(CmsSubPackageCreateRequest request) {
        String url = cmsServiceUrl + "/sub-packages";
        log.info("Creating CMS sub-package: name={}, clientId={}", request.getName(), request.getClientId());
        try {
            ApiResponseDto<CmsSubPackageCreatedData> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsSubPackageCreatedData>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null
                    || response.getData().getId() == null || response.getData().getId().isBlank()) {
                throw new RuntimeException("CMS sub-package creation returned no id");
            }
            log.info("CMS sub-package created: id={}", response.getData().getId());
            return response.getData().getId();
        } catch (WebClientResponseException e) {
            log.error("CMS sub-package create failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create CMS sub-package: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating CMS sub-package", e);
            throw new RuntimeException("Failed to create CMS sub-package", e);
        }
    }

    public void updateSubPackage(String subPackageId, CmsSubPackageUpdateRequest request) {
        String url = cmsServiceUrl + "/sub-packages/" + subPackageId;
        log.info("Updating CMS sub-package: id={}, name={}", subPackageId, request.getName());
        try {
            webClient.put()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            log.info("CMS sub-package updated: id={}", subPackageId);
        } catch (WebClientResponseException e) {
            log.error("CMS sub-package update failed: id={}, status={}, body={}", subPackageId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to update CMS sub-package: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Unexpected error updating CMS sub-package id={}", subPackageId, e);
            throw new RuntimeException("Failed to update CMS sub-package", e);
        }
    }

    public CmsSubPackageData getSubPackageById(String subPackageId) {
        String url = cmsServiceUrl + "/sub-packages/" + subPackageId;
        log.info("Fetching CMS sub-package: id={}", subPackageId);
        try {
            ApiResponseDto<CmsSubPackageData> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsSubPackageData>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null
                    || response.getData().getId() == null || response.getData().getId().isBlank()) {
                throw new RuntimeException("CMS sub-package not found for id: " + subPackageId);
            }
            log.info("Fetched CMS sub-package: id={}, name={}", response.getData().getId(), response.getData().getName());
            return response.getData();
        } catch (WebClientResponseException e) {
            log.error("CMS sub-package fetch failed: id={}, status={}, body={}", subPackageId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch CMS sub-package: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching CMS sub-package id={}", subPackageId, e);
            throw new RuntimeException("Failed to fetch CMS sub-package", e);
        }
    }

    /**
     * Fetches CMS {@code GET .../topics/{id}}. Returns empty when missing or on non-fatal errors (warn only).
     */
    public Optional<CmsTopicDetailData> fetchTopicById(String topicId) {
        if (topicId == null || topicId.isBlank()) {
            return Optional.empty();
        }
        String trimmed = topicId.trim();
        String url = cmsServiceUrl + "/topics/" + trimmed;
        log.debug("Fetching CMS topic: id={}", trimmed);
        try {
            ApiResponseDto<CmsTopicDetailData> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsTopicDetailData>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null
                    || response.getData().getId() == null || response.getData().getId().isBlank()) {
                log.warn("CMS topic fetch returned no data: id={}", trimmed);
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (WebClientResponseException e) {
            log.warn("CMS topic fetch failed: id={}, status={}, body={}", trimmed, e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("CMS topic fetch failed: id={}", trimmed, e);
            return Optional.empty();
        }
    }
}
