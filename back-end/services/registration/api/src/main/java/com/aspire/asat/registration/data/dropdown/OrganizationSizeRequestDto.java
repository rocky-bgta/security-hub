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
public class OrganizationSizeRequestDto {
    
    @NotBlank(message = "Organization size name is required")
    private String name;
    
    @NotBlank(message = "Range is required")
    private String range;
}