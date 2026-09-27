package com.aspire.asat.cms.dto.subPackage;

import com.aspire.asat.cms.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPackageUpdateDto {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(min = 2, message = "At least 2 topic IDs are required")
    private List<String> topicId;

    private SubPackageStatus status;

    private String clientAdminId;
    private SubPackageAssignedFor assignedFor;
    private String channel;

    private Boolean isTrial; // Flag to indicate if this is a trial package
    private Boolean showInSite; // Flag to indicate if package should be shown on site
}
