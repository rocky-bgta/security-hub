package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "organization_dashboard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDashboard {

    @Id
    private String id;

    @Indexed(unique = true)
    private String organizationAdminId;

    private Integer totalProduct;

    private Integer totalPackage;

    private Integer totalLicense;

    private Integer totalClient;

    private Integer totalMsp;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}

