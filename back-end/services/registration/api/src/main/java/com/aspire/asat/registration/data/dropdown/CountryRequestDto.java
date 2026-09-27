package com.aspire.asat.registration.data.dropdown;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryRequestDto {
    
    @NotBlank(message = "Country code is required")
    private String code;

    @NotBlank(message = "Phone code is required")
    private String phoneCode;
    
    @NotBlank(message = "Country name is required")
    private String name;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    @Builder.Default
    private Boolean active = true;
}
