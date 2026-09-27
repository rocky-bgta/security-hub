package com.aspire.asat.registration.model.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "organization_type")
public class OrganizationType {

    @Id
    private String id;
    
    private String name;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy ;
    
    private String updatedBy;
    
    private Boolean isActive ;
}
