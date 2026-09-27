package com.aspire.asat.universal.data.externalresponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceDto {
    private String id;
    private String complianceName;
    private String acronym;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
}

