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
public class MspTypeRequestDto {

    @NotBlank(message = "MspType name is required")
    private String name;

    @Builder.Default
    private Boolean isActive = true;
}
