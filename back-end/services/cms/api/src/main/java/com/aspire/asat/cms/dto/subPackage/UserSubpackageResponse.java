package com.aspire.asat.cms.dto.subPackage;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserSubpackageResponse {

    private String productId;
    private String productName;
    private String subPackageId;
    private String subPackageName;
    private Long topicCount;

    private double progress;
    private String status;

    private Instant assignedAt;
    private Instant expiryDate;
}
