package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sub_packages")
public class SubPackage {

    @Id
    private String id;
    private String name;
    private String description;
    private String productId;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private String clientAdminId;
    private List<String> topicId;
    private String createdBy;
    private String updatedBy;
    private SubPackageStatus status;
    private String assignedFor;
    private String channel;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted = false;
    private Boolean isTrial = false;
    private Boolean showInSite = false;
    private Boolean isAlreadyAssigned = false;
    private Boolean isPhishingSubpackage;

}