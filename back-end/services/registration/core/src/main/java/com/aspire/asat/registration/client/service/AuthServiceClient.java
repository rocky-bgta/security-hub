package com.aspire.asat.registration.client.service;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.auth.request.LogoutRequestDto;
import com.aspire.asat.registration.data.auth.request.PasswordResetRequestDto;
import com.aspire.asat.registration.data.auth.response.PasswordResetTokenResponseDto;
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

import java.time.Duration;

/**
 * Client for interacting with Auth service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    @Value("${service.auth.url}")
    private String authServiceUrl;

    private final WebClient webClient;

    /**
     * Generate password reset token from Auth service
     *
     * @param requestDto password reset request containing username
     * @return the generated password reset token, or null if generation failed
     */
    public PasswordResetTokenResponseDto generatePasswordResetToken(PasswordResetRequestDto requestDto) {
        if (requestDto == null || requestDto.getUsername() == null || requestDto.getUsername().trim().isEmpty()) {
            log.warn("Password reset request is null or username is empty");
            return null;
        }

        try {
            log.info("Generating password reset token from Auth service for username: {}", requestDto.getUsername());

            String url = authServiceUrl + "/password/generate-token";

            ApiResponseDto<PasswordResetTokenResponseDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<PasswordResetTokenResponseDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Successfully generated password reset token for username: {}", requestDto.getUsername());
                return response.getData();
            } else {
                log.warn("Password reset token generation returned null response for username: {}", requestDto.getUsername());
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("Error generating password reset token from Auth service for username: {}. Status: {}, Response: {}",
                    requestDto.getUsername(), e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error generating password reset token from Auth service for username: {}",
                    requestDto.getUsername(), e);
            return null;
        }
    }

    /**
     * Force-logout a user via Auth service force-logout API (revokes Redis session + refresh tokens).
     * Best-effort: logs errors and returns false on failure so callers are not blocked.
     *
     * @param userId AspireUser userId to logout
     * @return true if Auth accepted the logout, false otherwise
     */
    public boolean logoutUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            log.warn("Cannot force logout: userId is null or empty");
            return false;
        }

        try {
            log.info("Calling Auth force-logout for userId: {}", userId);

            String url = authServiceUrl + "/force-logout";
            LogoutRequestDto requestDto = LogoutRequestDto.builder().userId(userId.trim()).build();

            ApiResponseDto<Boolean> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<Boolean>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            boolean success = response != null && Boolean.TRUE.equals(response.getData());
            if (success) {
                log.info("Successfully forced logout for userId: {}", userId);
            } else {
                log.warn("Auth force-logout returned unsuccessful response for userId: {}, response: {}", userId, response);
            }
            return success;
        } catch (WebClientResponseException e) {
            log.error("Error calling Auth force-logout for userId: {}. Status: {}, Response: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error calling Auth force-logout for userId: {}", userId, e);
            return false;
        }
    }
}

