package com.aspire.asat.registration.data.systemUser.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SystemUserRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String designation;
    private String department;
    private String country;
    private String zipCode;
    private String supervisorName;

    @NotNull(message = "Role IDs are required")
    private List<String> roleIds;
}
