package com.aspire.asat.cms.dto.basePackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePackageConfigUpdateRequest {
    
    @NotBlank(message = "Name cannot be blank")
    private String name;
}
