package com.aspire.asat.cms.mapper;

import com.aspire.asat.cms.dto.topic.CountryDetailsDto;
import com.aspire.asat.cms.dto.topic.RegistrationCountryRespDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility to convert country data between different formats
 */
@Component
public class CountryMapper {

    /**
     * Convert Registration service country DTO to CMS service country details DTO
     * @param registrationCountry Registration service country DTO
     * @return CMS service country details DTO
     */
    public CountryDetailsDto toCountryDetailsDto(RegistrationCountryRespDto registrationCountry) {
        if (registrationCountry == null) {
            return null;
        }
        
        return CountryDetailsDto.builder()
                .id(registrationCountry.getId())
                .countryName(registrationCountry.getName())
                .countryCode(registrationCountry.getCode())
                .sortOrder(registrationCountry.getDisplayOrder())
                .isActive(registrationCountry.getActive())
                .createdAt(registrationCountry.getCreatedAt())
                .updatedAt(registrationCountry.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of Registration service country DTOs to CMS service country details DTOs
     * @param registrationCountries List of Registration service country DTOs
     * @return List of CMS service country details DTOs
     */
    public List<CountryDetailsDto> toCountryDetailsDtoList(List<RegistrationCountryRespDto> registrationCountries) {
        if (registrationCountries == null) {
            return List.of();
        }
        
        return registrationCountries.stream()
                .map(this::toCountryDetailsDto)
                .collect(Collectors.toList());
    }

    /**
     * Filter countries by topic's country IDs
     * @param allCountries List of all countries
     * @param topicCountryIds List of country IDs associated with the topic
     * @return Filtered list of countries that match the topic's country IDs
     */
    public List<CountryDetailsDto> filterCountriesByTopic(List<CountryDetailsDto> allCountries, List<String> topicCountryIds) {
        if (allCountries == null || topicCountryIds == null || topicCountryIds.isEmpty()) {
            return List.of();
        }
        
        return allCountries.stream()
                .filter(country -> topicCountryIds.contains(country.getId()))
                .collect(Collectors.toList());
    }
}
