package com.aspire.asat.registration.data.branding.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingUpdateRequestDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String logoFilePath;

    private Boolean active;
}