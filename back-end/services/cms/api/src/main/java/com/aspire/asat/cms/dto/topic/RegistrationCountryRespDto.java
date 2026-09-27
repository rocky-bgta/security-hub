package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for country data from Registration service
 * This matches the Registration service's CountryRespDto structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCountryRespDto {
    private String id;
    private String code;
    private String name;
    private Integer displayOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
