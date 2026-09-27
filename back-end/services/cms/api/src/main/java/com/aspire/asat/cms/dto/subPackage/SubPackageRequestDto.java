package com.aspire.asat.cms.dto.subPackage;

import com.aspire.asat.cms.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPackageRequestDto {

    @NotBlank(message = "SubPackage name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Package ID is required")
    private String packageId;

    @NotBlank(message = "Product Package ID is required")
    private String productPackageId;

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client Admin ID is required")
    private String clientAdminId;

    @NotNull(message = "Topic IDs are required")
    @Size(min = 2, message = "At least 2 topic IDs are required")
    private List<String> topicId;

    @NotBlank(message = "Created by is required")
    private String createdBy;

    @NotNull(message = "Status is required")
    private SubPackageStatus status = SubPackageStatus.ACTIVE; // Default to ACTIVE

    private SubPackageAssignedFor assignedFor;

    private String channel;

    private Boolean isTrial = false; // Flag to indicate if this is a trial package
    private Boolean showInSite = false; // Flag to indicate if package should be shown on site
    private Boolean isPhishingSubpackage = false;
}
