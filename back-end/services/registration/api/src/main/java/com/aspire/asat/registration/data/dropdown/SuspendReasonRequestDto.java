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
public class SuspendReasonRequestDto {
    
    @NotBlank(message = "Suspend reason name is required")
    private String name;
    
    private String description;
    
    @Builder.Default
    private Boolean active = true;
}

