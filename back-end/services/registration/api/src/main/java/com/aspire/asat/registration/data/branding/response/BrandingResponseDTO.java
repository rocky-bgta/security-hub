package com.aspire.asat.registration.data.branding.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingResponseDTO {

    private String id;
    private String companyName;
    private String logoFilePath;
    private String clientAdminId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private Boolean active;
}

