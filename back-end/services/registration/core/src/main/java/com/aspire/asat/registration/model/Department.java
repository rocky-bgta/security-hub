package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departments")
public class Department {

    @Id
    private String id;

    private String name;
    private String description;
    private String clientAdminId; // ID of the client admin this department belongs to
    private Boolean isSystemDefined; // Whether the department is system-defined or user-defined


    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy; // ID of the user who created this department
    private boolean active; // Whether the department is active or not

}
