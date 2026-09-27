package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AvailablePackageDTO {
    private String packageId;
    private String packageName;
    private String packageDescription;
    private String productName;
    private List<String> featureList;
}
