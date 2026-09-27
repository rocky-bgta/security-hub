package com.aspire.asat.registration.data.roles;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {

    @NotBlank(message = "Role name cannot be empty")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String roleName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Access level is required")
    @Min(value = 1, message = "Access level must be a positive number")
    private Integer accessLevel;

    private String colorTheme;

    @NotNull(message = "Status is required")
    private String status; // "ACTIVE" or "INACTIVE"
}
