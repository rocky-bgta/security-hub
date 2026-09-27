package com.aspire.asat.registration.data.creditAllocation;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CreditAllocationReasonResponseDTO {
    private String id;
    private String reasonName;
    private String description;
    private Boolean isActive;
    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;
}

