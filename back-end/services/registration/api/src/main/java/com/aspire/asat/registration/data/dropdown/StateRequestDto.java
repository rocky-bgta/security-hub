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
public class StateRequestDto {
    
    @NotBlank(message = "Country ID is required")
    private String countryId;
    
    @NotBlank(message = "State code is required")
    private String code;
    
    @NotBlank(message = "State name is required")
    private String name;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    @Builder.Default
    private Boolean active = true;
}
