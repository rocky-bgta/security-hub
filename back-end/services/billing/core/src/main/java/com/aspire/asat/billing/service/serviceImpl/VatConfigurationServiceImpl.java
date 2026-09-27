package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.RegionVatConfig;
import com.aspire.asat.billing.dto.VatConfigurationCreateDTO;
import com.aspire.asat.billing.dto.VatConfigurationResponseDTO;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.billing.exception.ResourceNotFoundException;
import com.aspire.asat.billing.model.VatConfiguration;
import com.aspire.asat.billing.repo.VatConfigurationRepository;
import com.aspire.asat.billing.service.VatConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VatConfigurationServiceImpl implements VatConfigurationService {

    private final VatConfigurationRepository vatRepository;
    private final WebClient webClient;

    @Value("${service.registration.url}")
    private String registrationServiceUrl;

    @Override
    public VatConfigurationResponseDTO createVatConfiguration(VatConfigurationCreateDTO dto) {
        VatConfiguration config = mapToModel(dto);
        config.setCreatedAt(Instant.now());
        config.setUpdatedAt(Instant.now());
        return mapToDto(vatRepository.save(config));
    }

    @Override
    public VatConfigurationResponseDTO updateVatConfiguration(String countryId, VatConfigurationCreateDTO dto) {
        VatConfiguration config = mapToModel(dto);
        config.setId(countryId);
        config.setUpdatedAt(Instant.now());
        return mapToDto(vatRepository.save(config));
    }

    @Override
    public VatConfigurationResponseDTO getVatByCountryId(String countryId) {
        return vatRepository.findById(countryId)
                .map(this::mapToDto)
                .orElseGet(() -> buildDefaultVatConfiguration(countryId));
    }

    @Override
    public List<VatConfigurationResponseDTO> getAllVatConfigurations(int offset, int limit) {
        // Validate limit is positive
        if (limit <= 0) {
            throw new BillingServiceException(
                "Limit must be greater than 0", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate offset is non-negative
        if (offset < 0) {
            throw new BillingServiceException(
                "Offset must be greater than or equal to 0", 
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Convert offset (page number) to actual page for PageRequest
        // offset=0 → page 0, offset=1 → page 1, offset=2 → page 2
        Pageable pageable = PageRequest.of(offset, limit);
        return vatRepository.findAll(pageable).stream().map(this::mapToDto).toList();
    }

    @Override
    public long countVatConfigurations() {
        return vatRepository.count();
    }

    @Override
    public void deleteVatConfiguration(String countryId) {
        if (!vatRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("VAT configuration not found for countryId: " + countryId);
        }
        vatRepository.deleteById(countryId);
    }

    private VatConfiguration mapToModel(VatConfigurationCreateDTO dto) {
        return VatConfiguration.builder()
                .id(dto.getId())
                .countryName(dto.getCountryName())
                .defaultVatRate(dto.getDefaultVatRate())
                .regionBased(dto.isRegionBased())
                .regions(dto.getRegions())
                .build();
    }

    private VatConfigurationResponseDTO mapToDto(VatConfiguration config) {
        return new VatConfigurationResponseDTO(
                config.getId(),
                config.getCountryName(),
                config.getDefaultVatRate(),
                config.isRegionBased(),
                config.getRegions(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }

    private VatConfigurationResponseDTO buildDefaultVatConfiguration(String countryId) {
        String countryName = fetchCountryName(countryId);
        Instant now = Instant.now();
        return new VatConfigurationResponseDTO(
                countryId,
                countryName,
                0.0,
                false,
                List.of(),
                now,
                now
        );
    }

    private String fetchCountryName(String countryId) {
        String url = registrationServiceUrl + "/dropdown/countries/" + countryId;
        try {
            JsonNode responseJson = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (responseJson == null) {
                throw new BillingServiceException(
                        "Country lookup returned null response for countryId: " + countryId,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            JsonNode dataNode = responseJson.get("data");
            if (dataNode == null || dataNode.isNull()) {
                throw new ResourceNotFoundException("Country not found for countryId: " + countryId);
            }

            JsonNode nameNode = dataNode.get("name");
            if (nameNode == null || nameNode.isNull() || nameNode.asText().isBlank()) {
                throw new BillingServiceException(
                        "Country name missing in registration response for countryId: " + countryId,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            return nameNode.asText();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Country not found for countryId: " + countryId);
            }
            throw new BillingServiceException(
                    "Failed to fetch country details from registration service for countryId: " + countryId,
                    HttpStatus.BAD_GATEWAY
            );
        } catch (ResourceNotFoundException | BillingServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new BillingServiceException(
                    "Failed to fetch country details from registration service for countryId: " + countryId,
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    @Override
    public double getVatRate(String id, String stateId) {
        VatConfiguration config = vatRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("VAT configuration not found for id: " + id));

        if (config.isRegionBased() && stateId != null && !stateId.isBlank()) {
            return config.getRegions().stream()
                    .filter(region -> region.getId().equals(stateId))
                    .map(RegionVatConfig::getVatRate)
                    .findFirst()
                    .orElse(config.getDefaultVatRate());
        }

        return config.getDefaultVatRate();
    }

    @Override
    public double getVatRateForRegion(String countryId, String regionId) {
        VatConfiguration config = vatRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country VAT config not found for countryId: " + countryId));

        if (config.isRegionBased() && regionId != null && !regionId.isBlank()) {
            return config.getRegions().stream()
                    .filter(r -> r.getId().equals(regionId.trim()))
                    .map(RegionVatConfig::getVatRate)
                    .findFirst()
                    .orElse(config.getDefaultVatRate());
        }

        return config.getDefaultVatRate();
    }

}
