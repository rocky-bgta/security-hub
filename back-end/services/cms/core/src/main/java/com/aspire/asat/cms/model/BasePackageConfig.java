package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "base_package_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePackageConfig {
    
    @Id
    private String basePackageId;
    
    private String name;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    @Builder.Default
    private Boolean active = true;
}
