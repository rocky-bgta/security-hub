package com.aspire.asat.registration.data.requiredinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for required info API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequiredInfoResponseDTO {
    private Boolean hasBranding;
    private Boolean hasUser;
    private Boolean productAssigned;
    private Boolean hasCertificateTemplate;
}

