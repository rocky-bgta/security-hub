package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCoursePageDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseStatisticsCountsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * Calls the CMS phishing course statistics endpoints (under {@code /client/phishing-course}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsPhishingCourseClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final String STATISTICS_PATH = "/client/phishing-course/statistics";
    private static final String DETAILS_PATH = "/client/phishing-course/details";

    private final WebClient webClient;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    public CmsPhishingCourseStatisticsCountsDto getStatistics(String clientAdminId) {
        return getStatistics(clientAdminId, null);
    }

    public CmsPhishingCourseStatisticsCountsDto getStatistics(
            String clientAdminId, Collection<String> subPackageIds) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(cmsServiceUrl + STATISTICS_PATH)
                .queryParam("clientAdminId", clientAdminId);
        if (subPackageIds != null && !subPackageIds.isEmpty()) {
            for (String id : subPackageIds) {
                if (id != null && !id.isBlank()) {
                    builder.queryParam("subPackageIds", id.trim());
                }
            }
        }
        String url = builder.toUriString();
        log.info("Fetching CMS phishing course statistics for clientAdminId={}, subPackageIdsCount={}",
                clientAdminId, subPackageIds == null ? 0 : subPackageIds.size());
        try {
            ApiResponseDto<CmsPhishingCourseStatisticsCountsDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsPhishingCourseStatisticsCountsDto>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                log.warn("CMS phishing course statistics returned no data for clientAdminId={}", clientAdminId);
                return CmsPhishingCourseStatisticsCountsDto.builder().build();
            }
            return response.getData();
        } catch (WebClientResponseException e) {
            log.error("CMS phishing course statistics failed: clientAdminId={}, status={}, body={}",
                    clientAdminId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch phishing course statistics from CMS", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching CMS phishing course statistics for clientAdminId={}", clientAdminId, e);
            throw new RuntimeException("Failed to fetch phishing course statistics from CMS", e);
        }
    }

    public CmsPhishingCoursePageDto getDetails(String clientAdminId, int offset, int pageSize, Collection<String> userIds) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(cmsServiceUrl + DETAILS_PATH)
                .queryParam("clientAdminId", clientAdminId)
                .queryParam("offset", offset)
                .queryParam("pageSize", pageSize);
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {
                if (userId != null && !userId.isBlank()) {
                    builder.queryParam("userIds", userId.trim());
                }
            }
        }
        String url = builder.toUriString();

        int userIdsCount = userIds == null ? 0 : userIds.size();
        log.info("Fetching CMS phishing course details for clientAdminId={}, offset={}, pageSize={}, userIdsCount={}",
                clientAdminId, offset, pageSize, userIdsCount);
        try {
            ApiResponseDto<CmsPhishingCoursePageDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsPhishingCoursePageDto>>() {})
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                log.warn("CMS phishing course details returned no data for clientAdminId={}", clientAdminId);
                return CmsPhishingCoursePageDto.builder()
                        .offset(offset)
                        .pageSize(pageSize)
                        .total(0L)
                        .items(List.of())
                        .build();
            }
            return response.getData();
        } catch (WebClientResponseException e) {
            log.error("CMS phishing course details failed: clientAdminId={}, status={}, body={}",
                    clientAdminId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch phishing course details from CMS", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching CMS phishing course details for clientAdminId={}", clientAdminId, e);
            throw new RuntimeException("Failed to fetch phishing course details from CMS", e);
        }
    }
}
