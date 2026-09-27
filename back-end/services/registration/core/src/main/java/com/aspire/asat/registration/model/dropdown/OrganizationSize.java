package com.aspire.asat.registration.model.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "organization_sizes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSize {
    
    @Id
    private String id;
    private String name;
    private String range;
    private Instant createdAt;
    private Instant updatedAt;
}