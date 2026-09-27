package com.aspire.asat.cms.client;

import com.aspire.asat.cms.client.dto.PhishingTrainingRiskScoreRequestDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

/**
 * Client for calling Phishing module APIs from CMS.
 * Calls POST /api/v1/phishing/training-risk-score/{userId} to update user training risk score.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PhishingClient {

    private static final String TRAINING_RISK_SCORE_PATH = "/phishing/training-risk-score";
    private static final ParameterizedTypeReference<ApiResponseDto<Double>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    @Value("${service.phishing.url:}")
    private String phishingBaseUrl;

    /**
     * Calls Phishing POST /api/v1/phishing/training-risk-score/{userId} to update the user's training risk score.
     *
     * @param userId  user ID (path variable)
     * @param request body with clientAdminId and riskScore
     * @return optional containing the updated risk score (data) if successful, empty on failure or if URL not configured
     */
    public Optional<Double> updateTrainingRiskScore(String userId, PhishingTrainingRiskScoreRequestDto request) {
        if (phishingBaseUrl == null || phishingBaseUrl.isBlank()) {
            log.warn("Phishing base URL (service.phishing.url) is not configured, skipping updateTrainingRiskScore");
            return Optional.empty();
        }
        if (userId == null || userId.isBlank()) {
            log.warn("userId is required for updateTrainingRiskScore");
            return Optional.empty();
        }
        if (request == null || request.getClientAdminId() == null || request.getClientAdminId().isBlank()) {
            log.warn("request with clientAdminId is required for updateTrainingRiskScore");
            return Optional.empty();
        }

        String url = phishingBaseUrl.replaceAll("/$", "") + TRAINING_RISK_SCORE_PATH + "/" + userId;
        log.debug("Calling Phishing API: POST {}", url);

        try {
            ApiResponseDto<Double> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RESPONSE_TYPE)
                    .block();

            if (response == null) {
                log.warn("Phishing training-risk-score returned null response");
                return Optional.empty();
            }
            if (response.getStatusCode() != 200) {
                log.warn("Phishing training-risk-score returned statusCode={}, message={}", response.getStatusCode(), response.getMessage());
                return Optional.empty();
            }
            log.debug("Phishing training-risk-score updated for userId={}, riskScore={}", userId, response.getData());
            return Optional.ofNullable(response.getData());
        } catch (WebClientResponseException e) {
            log.error("Phishing API error for training-risk-score: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling Phishing updateTrainingRiskScore for userId={}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
