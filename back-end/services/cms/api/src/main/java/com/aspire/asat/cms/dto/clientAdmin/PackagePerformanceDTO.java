package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PackagePerformanceDTO {
    private String packageName;
    private String productName;
    private int assignedUsers;
    private int licensesUsed;
    private int topicsCompleted;
    private String completionRate;     // e.g., "72%"
    private String avgTimeSpent;       // e.g., "3.5 hours"
    private int certifications;
    private String certificationRate;  // e.g., "54%"
}
