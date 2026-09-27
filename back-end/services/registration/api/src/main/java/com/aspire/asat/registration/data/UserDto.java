package com.aspire.asat.registration.data;

import com.aspire.asat.registration.data.enums.Role;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.utils.PasswordSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @JsonIgnore
    @JsonSerialize(using = PasswordSerializer.class)
    private String password;

    private String companyName;
    private String designation;
    private Role role;
    private String group;
    private String department;
    private String country;
    private String zipCode;
    private String supervisorName;
    private String supervisorEmail;
    private UserStatus userStatus;
    private String logo;

}
