package com.aspire.asat.cms.dto.basePackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePackageConfigResponse {
    
    private String basePackageId;
    
    private String name;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    private Boolean active;
}
