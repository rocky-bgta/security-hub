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
public class LanguageRequestDto {
    
    @NotBlank(message = "Language code is required")
    private String code;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    @Builder.Default
    private Boolean active = true;
}
