package com.aspire.asat.registration.data.branding.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandingCreateRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @Size(max = 500, message = "Logo file path must not exceed 500 characters")
    private String logoFilePath;

}

