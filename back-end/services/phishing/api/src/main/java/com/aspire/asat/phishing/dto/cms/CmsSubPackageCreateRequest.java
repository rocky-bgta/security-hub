package com.aspire.asat.phishing.dto.cms;

import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JSON body for {@code POST /cms/api/v1/sub-packages}. Kept local to phishing (no CMS module dependency).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsSubPackageCreateRequest {

    private String name;
    private String description;
    private String productId;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private String clientAdminId;
    private List<String> topicId;
    private String createdBy;
    private String status;
    private SubPackageAssignedFor assignedFor;
    private Boolean isTrial;
    private Boolean showInSite;
    private Boolean isPhishingSubpackage;
    private String channel;
}
