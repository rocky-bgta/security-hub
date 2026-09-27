package com.aspire.asat.registration.data.netTerm;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NetTermConfigurationResponseDTO {
    private String id;
    private String netTermName;
    private Integer netTermInDays;
    private Boolean isActive;
    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;
}

