package com.aspire.asat.registration.client.service;

import com.aspire.asat.common.constants.InternalServiceAuthConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.phishing.request.UserLicenceSnapshotRequestDto;
import com.aspire.asat.registration.data.phishing.request.UserRiskProfileSaveRequestDto;
import com.aspire.asat.registration.data.phishing.response.CampaignLicenseUsageDto;
import com.aspire.asat.registration.data.phishing.response.UserRiskSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Client for Phishing service APIs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhishingServiceClient {

    private static final String USER_RISK_PROFILES_PATH = "/phishing/user-risk-profiles";
    private static final String CAMPAIGN_LICENSE_USAGE_PATH = "/phishing/internal/campaign-license-usage";
    private static final String LICENSED_USER_IDS_PATH = "/phishing/internal/licensed-user-ids";
    private static final String USER_LICENCE_SNAPSHOT_PATH = "/phishing/internal/user-licence-snapshot";

    @Value("${service.phishing.url:}")
    private String phishingServiceUrl;

    @Value("${internal.service.api-key:}")
    private String internalServiceApiKey;

    private final WebClient webClient;

    /**
     * Saves a user risk profile via the Phishing service.
     *
     * @param request the profile data to save (clientId and userId required)
     * @return the saved profile as UserRiskSummaryDto, or null on failure or if URL is not configured
     */
    public UserRiskSummaryDto saveUserRiskProfile(UserRiskProfileSaveRequestDto request) {
        if (phishingServiceUrl == null || phishingServiceUrl.isBlank()) {
            log.debug("Phishing service URL is not configured; skipping save user risk profile");
            return null;
        }
        if (request == null || request.getClientId() == null || request.getUserId() == null) {
            log.warn("Save user risk profile skipped: request, clientId or userId is null");
            return null;
        }

        String url = phishingServiceUrl.trim().replaceAll("/$", "") + USER_RISK_PROFILES_PATH;

        try {
            log.debug("Calling Phishing save user risk profile: clientId={}, userId={}", request.getClientId(), request.getUserId());

            ApiResponseDto<UserRiskSummaryDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<UserRiskSummaryDto>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response != null && response.getData() != null) {
                log.debug("Saved user risk profile via Phishing service for clientId={}, userId={}",
                        request.getClientId(), request.getUserId());
                return response.getData();
            }
            log.warn("Phishing save user risk profile returned null or empty data");
            return null;

        } catch (WebClientResponseException e) {
            log.error("Phishing save user risk profile failed. Status: {}, Response: {}, clientId={}, userId={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), request.getClientId(), request.getUserId());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error calling Phishing save user risk profile for clientId={}, userId={}",
                    request.getClientId(), request.getUserId(), e);
            return null;
        }
    }

    /**
     * Fetches unique campaign-participant counts per productPackageId from phishing.
     *
     * @param clientId client admin id (required)
     * @return usage rows; never null
     * @throws IllegalStateException if phishing URL is missing or the call fails
     */
    public List<CampaignLicenseUsageDto> getCampaignLicenseUsage(String clientId) {
        if (!StringUtils.hasText(phishingServiceUrl)) {
            throw new IllegalStateException("Phishing service URL is not configured");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("clientId is required for campaign license usage");
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(phishingServiceUrl.trim().replaceAll("/$", "") + CAMPAIGN_LICENSE_USAGE_PATH)
                .queryParam("clientId", clientId.trim())
                .toUriString();

        try {
            log.debug("Calling Phishing campaign license usage: clientId={}", clientId);

            ApiResponseDto<List<CampaignLicenseUsageDto>> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(InternalServiceAuthConstants.HEADER_NAME, internalServiceApiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<List<CampaignLicenseUsageDto>>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }
            return response.getData();
        } catch (WebClientResponseException e) {
            log.error("Phishing campaign license usage failed. Status: {}, Response: {}, clientId={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), clientId);
            throw new IllegalStateException("Failed to retrieve campaign license usage from phishing service", e);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Phishing campaign license usage for clientId={}",
                    clientId, e);
            throw new IllegalStateException("Failed to retrieve campaign license usage from phishing service", e);
        }
    }

    /**
     * Licensed userIds for a client + product package (Add-licence exclude list).
     *
     * @throws IllegalStateException if phishing URL is missing or the call fails
     */
    public List<String> getLicensedUserIds(String clientId, String productPackageId) {
        if (!StringUtils.hasText(phishingServiceUrl)) {
            throw new IllegalStateException("Phishing service URL is not configured");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("clientId is required for licensed user IDs");
        }
        if (!StringUtils.hasText(productPackageId)) {
            throw new IllegalArgumentException("productPackageId is required for licensed user IDs");
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(phishingServiceUrl.trim().replaceAll("/$", "") + LICENSED_USER_IDS_PATH)
                .queryParam("clientId", clientId.trim())
                .queryParam("productPackageId", productPackageId.trim())
                .toUriString();

        try {
            log.debug("Calling Phishing licensed user IDs: clientId={}, productPackageId={}",
                    clientId, productPackageId);

            ApiResponseDto<List<String>> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(InternalServiceAuthConstants.HEADER_NAME, internalServiceApiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<List<String>>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }
            return response.getData();
        } catch (WebClientResponseException e) {
            log.error("Phishing licensed user IDs failed. Status: {}, Response: {}, clientId={}, productPackageId={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), clientId, productPackageId);
            throw new IllegalStateException("Failed to retrieve licensed user IDs from phishing service", e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Phishing licensed user IDs for clientId={}, productPackageId={}",
                    clientId, productPackageId, e);
            throw new IllegalStateException("Failed to retrieve licensed user IDs from phishing service", e);
        }
    }

    /**
     * Syncs Registration end-user profile fields onto phishing_user_licence rows.
     * Zero matched rows is success (user not licensed).
     *
     * @throws IllegalStateException if phishing URL is missing or the call fails
     */
    public void syncLicensedUserSnapshot(UserLicenceSnapshotRequestDto request) {
        if (!StringUtils.hasText(phishingServiceUrl)) {
            throw new IllegalStateException("Phishing service URL is not configured");
        }
        if (request == null || !StringUtils.hasText(request.getUserId())
                || !StringUtils.hasText(request.getClientAdminId())) {
            throw new IllegalArgumentException("userId and clientAdminId are required for licence snapshot sync");
        }

        String url = phishingServiceUrl.trim().replaceAll("/$", "") + USER_LICENCE_SNAPSHOT_PATH;

        try {
            log.debug("Calling Phishing licence snapshot sync: userId={}, clientAdminId={}",
                    request.getUserId(), request.getClientAdminId());

            webClient.put()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(InternalServiceAuthConstants.HEADER_NAME, internalServiceApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(15))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Phishing licence snapshot sync failed. Status: {}, Response: {}, userId={}, clientAdminId={}",
                    e.getStatusCode(), e.getResponseBodyAsString(),
                    request.getUserId(), request.getClientAdminId());
            throw new IllegalStateException("Failed to sync licensed user snapshot to phishing service", e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Phishing licence snapshot sync for userId={}, clientAdminId={}",
                    request.getUserId(), request.getClientAdminId(), e);
            throw new IllegalStateException("Failed to sync licensed user snapshot to phishing service", e);
        }
    }
}
