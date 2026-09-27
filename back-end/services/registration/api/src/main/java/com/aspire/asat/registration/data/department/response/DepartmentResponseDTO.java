package com.aspire.asat.registration.data.department.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDTO {

    private String id;
    private String name;
    private String description;
    private String clientAdminId;
    private Boolean isSystemDefined;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private Boolean active;
}
