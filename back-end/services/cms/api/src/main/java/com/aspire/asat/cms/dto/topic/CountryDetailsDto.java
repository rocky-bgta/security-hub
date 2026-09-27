package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for country details in topic response
 * This matches the expected API response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryDetailsDto {
    private String id;
    private String countryName;
    private String countryCode;
    private Integer sortOrder;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
