package com.aspire.asat.registration.data.cms.response;

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
    private String clientId;
    private String clientAdminId;
    private List<String> topicId;
    private String createdBy;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Long assignedUserCount;
    private Boolean isTrial;
    private Boolean showInSite;
    private Boolean isAlreadyAssigned;
}

