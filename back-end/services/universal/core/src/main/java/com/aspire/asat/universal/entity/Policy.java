package com.aspire.asat.universal.entity;

import com.aspire.asat.universal.enums.PolicyStatus;
import com.aspire.asat.universal.policy.FileAttachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "policies")
public class Policy {

    @Id
    private String id;
    private String policyName;
    private String policyTypeId;
    private LocalDate effectiveDate;
    private PolicyStatus status;
    private LocalDate policyEndDate;
    private String description;
    private List<FileAttachment> files;
    private String companyName;
    private Boolean isDefault = false;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String countryId;
    private String complianceId;
    private String industryId;
    private String clientAdminId;
    private String mspId;
}

