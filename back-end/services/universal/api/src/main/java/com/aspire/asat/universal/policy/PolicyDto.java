package com.aspire.asat.universal.policy;

import com.aspire.asat.universal.enums.PolicyStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyDto {
    private String id;
    private String policyName;
    private String policyTypeId;
    private String policyTypeName;
    private LocalDate effectiveDate;
    private PolicyStatus status;
    private LocalDate policyEndDate;
    private String description;
    private List<FileAttachment> files;
    private String industryId;
    private String companyName;
    private Boolean isDefault;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String countryId;
    private String complianceId;
    private String clientAdminId;
    private String mspId;

    /** True when the current MSP user can edit/delete this policy; omitted for other user types. */
    private Boolean editable;

    // Enriched fields from external APIs
    private CountryInfo country;
    private IndustryInfo industry;
    private ComplianceInfo compliance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryInfo {
        private String id;
        private String code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndustryInfo {
        private String id;
        private String code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceInfo {
        private String id;
        private String complianceName;
        private String description;
    }
}

