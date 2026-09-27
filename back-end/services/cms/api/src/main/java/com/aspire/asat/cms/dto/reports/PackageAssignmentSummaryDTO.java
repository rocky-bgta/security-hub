package com.aspire.asat.cms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageAssignmentSummaryDTO {
    private long totalPackages;
    private long totalSubPackages;
    private long activeAssignments;
    private long expiringSoon;
    private long expired;
    private long completeAssignments;
}
