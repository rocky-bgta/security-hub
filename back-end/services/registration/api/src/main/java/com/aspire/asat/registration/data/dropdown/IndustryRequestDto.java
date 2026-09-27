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
public class IndustryRequestDto {

    @NotBlank(message = "Organization type ID is required")
    private String organizationTypeId;
    
    @NotBlank(message = "Industry code is required")
    private String code;
    
    @NotBlank(message = "Industry name is required")
    private String name;
    
    @Builder.Default
    private Boolean active = true;
}
