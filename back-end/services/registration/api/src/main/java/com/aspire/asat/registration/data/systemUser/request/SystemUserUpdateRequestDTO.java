package com.aspire.asat.registration.data.systemUser.request;

import com.aspire.asat.registration.data.enums.UserStatus;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class SystemUserUpdateRequestDTO {

    private String firstName;
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String companyName;
    private String designation;
    private String supervisorName;
    private String department;

    private UserStatus status; // ACTIVE or INACTIVE
    private List<String> roleIds;
}
