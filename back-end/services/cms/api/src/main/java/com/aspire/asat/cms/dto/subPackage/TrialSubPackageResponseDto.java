package com.aspire.asat.cms.dto.subPackage;

import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialSubPackageResponseDto {

    private String id;
    private String name;
    private String description;
    private String productId;
    private String productName;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private String clientAdminId;
    private java.util.List<String> topicId;
    private String createdBy;
    private SubPackageStatus status;
    private String channel;
    private Instant createdAt;
    private Instant updatedAt;
    private Long assignedUserCount;
    private Boolean isTrial; // Flag to indicate if this is a trial package
    private Boolean showInSite; // Flag to indicate if package should be shown on site
}

