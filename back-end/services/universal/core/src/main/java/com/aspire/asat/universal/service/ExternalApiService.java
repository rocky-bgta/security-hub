package com.aspire.asat.universal.service;

import com.aspire.asat.universal.data.externalresponses.ClientMspIdDto;
import com.aspire.asat.universal.data.externalresponses.ComplianceDto;
import com.aspire.asat.universal.data.externalresponses.CountryDto;
import com.aspire.asat.universal.data.externalresponses.ExternalApiResponse;
import com.aspire.asat.universal.data.externalresponses.IndustryDto;
import com.aspire.asat.universal.data.externalresponses.SubPackageDetailResponseDto;
import com.aspire.asat.universal.data.externalresponses.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${client.registration.url}")
    private String registrationServiceUrl;

    @Value("${client.cms.url}")
    private String cmsServiceUrl;

    public Mono<CountryDto> getCountryById(String countryId) {
        if (countryId == null || countryId.isEmpty()) {
            return Mono.empty();
        }

        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        return webClient.get()
                .uri("/dropdown/countries/{id}", countryId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ExternalApiResponse<CountryDto>>() {})
                .map(ExternalApiResponse::getData)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Error fetching country with id: {}", countryId, e);
                    return Mono.empty();
                });
    }

    public Mono<IndustryDto> getIndustryById(String industryId) {
        if (industryId == null || industryId.isEmpty()) {
            return Mono.empty();
        }

        WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

        return webClient.get()
                .uri("/dropdown/industries/{id}", industryId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ExternalApiResponse<IndustryDto>>() {})
                .map(ExternalApiResponse::getData)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Error fetching industry with id: {}", industryId, e);
                    return Mono.empty();
                });
    }

    public Mono<ComplianceDto> getComplianceById(String complianceId) {
        if (complianceId == null || complianceId.isEmpty()) {
            return Mono.empty();
        }

        WebClient webClient = webClientBuilder.baseUrl(cmsServiceUrl).build();

        return webClient.get()
                .uri("/compliances/{id}", complianceId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ExternalApiResponse<ComplianceDto>>() {})
                .map(ExternalApiResponse::getData)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Error fetching compliance with id: {}", complianceId, e);
                    return Mono.empty();
                });
    }

    /**
     * Get MSP ID for a client admin from registration service
     * 
     * @param clientAdminId The client admin ID
     * @return Optional containing MSP ID if found, empty otherwise
     */
    public Optional<String> getMspIdByClientAdminId(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isEmpty()) {
            log.warn("Client admin ID is null or empty, cannot fetch MSP ID");
            return Optional.empty();
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

            ClientMspIdDto result = webClient.get()
                    .uri("/client/admin/{clientAdminId}/msp-id", clientAdminId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ExternalApiResponse<ClientMspIdDto>>() {})
                    .map(ExternalApiResponse::getData)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> {
                        log.error("Error fetching MSP ID for client admin: {}", clientAdminId, e);
                        return Mono.empty();
                    })
                    .block();

            if (result != null && result.getMspId() != null && !result.getMspId().isEmpty()) {
                log.info("Successfully retrieved MSP ID: {} for client admin: {}", result.getMspId(), clientAdminId);
                return Optional.of(result.getMspId());
            } else {
                log.warn("MSP ID not found for client admin: {}", clientAdminId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Exception while fetching MSP ID for client admin: {}", clientAdminId, e);
            return Optional.empty();
        }
    }

    /**
     * Get sub-package details by ID from CMS service
     * 
     * @param subPackageId The sub-package ID
     * @return Optional containing SubPackageDetailResponseDto if found, empty otherwise
     */
    public Optional<SubPackageDetailResponseDto> getSubPackageById(String subPackageId) {
        if (subPackageId == null || subPackageId.isEmpty()) {
            log.warn("Sub-package ID is null or empty, cannot fetch sub-package details");
            return Optional.empty();
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(cmsServiceUrl).build();

            SubPackageDetailResponseDto result = webClient.get()
                    .uri("/sub-packages/{id}", subPackageId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ExternalApiResponse<SubPackageDetailResponseDto>>() {})
                    .map(ExternalApiResponse::getData)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> {
                        log.error("Error fetching sub-package details for ID: {}", subPackageId, e);
                        return Mono.empty();
                    })
                    .block();

            if (result != null) {
                log.info("Successfully retrieved sub-package details for ID: {}", subPackageId);
                return Optional.of(result);
            } else {
                log.warn("Sub-package details not found for ID: {}", subPackageId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Exception while fetching sub-package details for ID: {}", subPackageId, e);
            return Optional.empty();
        }
    }

    /**
     * Get end user details by user ID from registration service
     * 
     * @param userId The user ID
     * @return Optional containing UserDetailsDto with email and name if found, empty otherwise
     */
    public Optional<UserDetailsDto> getEndUserDetails(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("User ID is null or empty, cannot fetch user details");
            return Optional.empty();
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(registrationServiceUrl).build();

            UserDetailsDto result = webClient.get()
                    .uri("/end-users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ExternalApiResponse<UserDetailsDto>>() {})
                    .map(ExternalApiResponse::getData)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> {
                        log.error("Error fetching end user details for ID: {}", userId, e);
                        return Mono.empty();
                    })
                    .block();

            if (result != null) {
                log.info("Successfully retrieved end user details for ID: {}", userId);
                return Optional.of(result);
            } else {
                log.warn("End user details not found for ID: {}", userId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Exception while fetching end user details for ID: {}", userId, e);
            return Optional.empty();
        }
    }
}

