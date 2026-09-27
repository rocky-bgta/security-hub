package com.aspire.asat.registration.data.cms.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPackageUpdateDto {
    private String status; // "INACTIVE", "ACTIVE", etc.
    private String clientAdminId;
    private Boolean isTrial;
    private Boolean showInSite;
}
