package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private String id; // MongoDB uses String IDs by default
    private String roleName;
    private String description;
    private Integer accessLevel; // 1 for basic, 2 for advanced, etc.
    private String colorTheme; // e.g., "#FF5733"
    private String status; // "ACTIVE" or "INACTIVE"
    private Instant createdAt; // Timestamp for creation
    private Instant updatedAt; // Timestamp for last update
}
