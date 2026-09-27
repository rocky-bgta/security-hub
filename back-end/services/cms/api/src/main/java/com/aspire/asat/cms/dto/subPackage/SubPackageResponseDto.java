package com.aspire.asat.cms.dto.subPackage;

import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPackageResponseDto {

    private String id;
    private String name;
    private String description;
    private String productId;
    private String productName;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private String clientAdminId;
    private List<String> topicId;
    private String createdBy;
    private SubPackageStatus status;
    private String assignedFor;
    private String channel;
    private Instant createdAt;
    private Instant updatedAt;
    private Long assignedUserCount; // Count of users assigned to this subpackage
    private Boolean isTrial; // Flag to indicate if this is a trial package
    private Boolean showInSite; // Flag to indicate if package should be shown on site
    private Boolean isAlreadyAssigned;
}
