package com.aspire.asat.registration.data;

import com.aspire.asat.registration.data.enums.Role;
import com.aspire.asat.registration.data.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UpdateUserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
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
    private MultipartFile png;

}
