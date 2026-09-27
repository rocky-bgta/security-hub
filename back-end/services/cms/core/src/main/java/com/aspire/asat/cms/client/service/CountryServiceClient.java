package com.aspire.asat.cms.client.service;

import com.aspire.asat.cms.dto.topic.RegistrationApiResponse;
import com.aspire.asat.cms.dto.topic.RegistrationCountryRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Client service to communicate with Registration service for country data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceClient {

    private final WebClient webClient;

    @Value("${service.registration.url}")
    private String registrationServiceUrl;

    /**
     * Fetch active countries from Registration service
     * @return List of active countries
     */
    public List<RegistrationCountryRespDto> getActiveCountries() {
        try {
            String url = registrationServiceUrl + "/dropdown/countries/active";
            log.info("Fetching active countries from Registration service: {}", url);
            
            RegistrationApiResponse<List<RegistrationCountryRespDto>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RegistrationApiResponse<List<RegistrationCountryRespDto>>>() {})
                    .block();
            
            List<RegistrationCountryRespDto> countries = null;
            if (response != null && response.getData() != null) {
                countries = response.getData();
                log.info("Successfully fetched {} active countries from Registration service", countries.size());
                
                if (!countries.isEmpty()) {
                    log.info("Sample country data: {}", countries.get(0));
                }
            } else {
                log.warn("No data received from Registration service or response is null");
            }
            
            return countries != null ? countries : List.of();
            
        } catch (Exception e) {
            log.error("Error fetching active countries from Registration service", e);
            return List.of();
        }
    }

    /**
     * Fetch all countries from Registration service
     * @return List of all countries
     */
    public List<RegistrationCountryRespDto> getAllCountries() {
        try {
            String url = registrationServiceUrl + "/dropdown/countries";
            log.info("Fetching all countries from Registration service: {}", url);
            
            RegistrationApiResponse<List<RegistrationCountryRespDto>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RegistrationApiResponse<List<RegistrationCountryRespDto>>>() {})
                    .block();
            
            List<RegistrationCountryRespDto> countries = null;
            if (response != null && response.getData() != null) {
                countries = response.getData();
                log.info("Successfully fetched {} countries from Registration service", countries.size());
            } else {
                log.warn("No data received from Registration service or response is null");
            }
            
            return countries != null ? countries : List.of();
            
        } catch (Exception e) {
            log.error("Error fetching countries from Registration service", e);
            return List.of();
        }
    }
}
