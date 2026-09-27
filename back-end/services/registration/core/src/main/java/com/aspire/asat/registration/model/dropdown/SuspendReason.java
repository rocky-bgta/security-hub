package com.aspire.asat.registration.model.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "suspend_reasons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendReason {
    
    @Id
    private String id;
    
    private String name;
    
    private String description;
    
    private Boolean active;
    
    private Instant createdAt;
    
    private String createdBy;
    
    private Instant updatedAt;
    
    private String updatedBy;
}

