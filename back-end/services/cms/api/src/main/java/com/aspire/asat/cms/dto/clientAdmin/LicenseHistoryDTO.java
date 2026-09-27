package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LicenseHistoryDTO {
    private String packageName;
    private String action;
    private String date;
    private int licensesAllocated;
    private String expiryDate;
    private String status;
}
