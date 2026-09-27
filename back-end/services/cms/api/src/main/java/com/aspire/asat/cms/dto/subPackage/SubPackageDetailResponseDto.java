package com.aspire.asat.cms.dto.subPackage;

import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
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
public class SubPackageDetailResponseDto {

    private String id;
    private String name;
    private String description;
    /** Product id (mirrors SubPackage entity); also reflected in productDetails. */
    private String productId;
    /** ProductPackage id (mirrors SubPackage entity); also reflected in packageDetails. */
    private String packageId;
    /** ClientProduct id for registration license validation (mirrors SubPackage entity). */
    private String productPackageId;
    private String clientId;
    private String clientAdminId;
    private String createdBy;
    private SubPackageStatus status;
    private String assignedFor;
    private String channel;
    private Instant createdAt;
    private Instant updatedAt;
    private Long assignedUserCount; // Count of users assigned to this subpackage
    private Boolean isTrial; // Flag to indicate if this is a trial package
    private Boolean showInSite; // Flag to indicate if package should be shown on site
    
    // Related details - using Object to avoid cross-module dependencies
    private Object productDetails;
    private Object packageDetails;
    private List<TopicRespDto> topicDetails;
    private Boolean isAlreadyAssigned;
}
