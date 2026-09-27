package com.aspire.asat.registration.data.dropdown;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubIndustryRequestDto {

    @NotBlank(message = "Organization type ID is required")
    private String organizationTypeId;

    @NotBlank(message = "Industry ID is required")
    private String industryId;

    @NotBlank(message = "Sub-industry code is required")
    private String code;

    @NotBlank(message = "Sub-industry name is required")
    private String name;

    @Builder.Default
    private Boolean active = true;
}
