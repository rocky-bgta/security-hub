package com.aspire.asat.common.dto.packages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for user assigned sub-packages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubPackageResponseDTO {

    private String id;
    private String userId;
    private String productId;
    private String subPackageId;

    private double progress; // 0 to 100
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    private Instant assignedAt;
    private Instant expiryDate;
}
