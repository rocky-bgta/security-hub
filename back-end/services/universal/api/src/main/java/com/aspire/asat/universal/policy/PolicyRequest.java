package com.aspire.asat.universal.policy;

import com.aspire.asat.universal.enums.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {
    private String policyName;
    private String policyTypeId;
    private LocalDate effectiveDate;
    private PolicyStatus status;
    private LocalDate policyEndDate;
    private String description;
    private List<FileAttachment> files;
    private String industryId;
    private String companyName;
    private String countryId;
    private String complianceId;
}

