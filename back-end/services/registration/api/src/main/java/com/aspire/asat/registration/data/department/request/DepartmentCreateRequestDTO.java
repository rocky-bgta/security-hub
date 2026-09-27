package com.aspire.asat.registration.data.department.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentCreateRequestDTO {

    @NotBlank(message = "Department name is required")
    @Size(max = 200, message = "Department name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean isSystemDefined = true; // Default to true as per requirement
}
