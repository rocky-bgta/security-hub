package com.aspire.asat.registration.data.department.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentUpdateRequestDTO {

    @NotBlank(message = "Department ID is required")
    private String id;

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean isSystemDefined;

    private Boolean active;
}
