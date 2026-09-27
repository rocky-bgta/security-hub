package com.aspire.asat.cms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageAssignmentLogRowDTO {
    private String user;
    private String packageName;
    private String subPackageName;
    private LocalDate assignedDate;
    private LocalDate expiryDate;
    private String status;
    private String assignedBy;
}
