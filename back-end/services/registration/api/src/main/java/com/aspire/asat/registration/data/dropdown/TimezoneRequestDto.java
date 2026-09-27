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
public class TimezoneRequestDto {
    
    @NotBlank(message = "Country ID is required")
    private String countryId;
    
    @NotBlank(message = "State ID is required")
    private String stateId;
    
    @NotBlank(message = "Timezone ID is required")
    private String timezoneId;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    @Builder.Default
    private Boolean active = true;
}
