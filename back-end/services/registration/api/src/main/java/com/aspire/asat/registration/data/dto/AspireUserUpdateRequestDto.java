package com.aspire.asat.registration.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import java.util.List;

/**
 * DTO for updating an existing AspireUser
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AspireUserUpdateRequestDto {

    private String firstName;
    private String lastName;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String password; // Encoded password
    private String phoneNumber;
    private String phoneCode;
    private String country;
    private String countryCode;
    private String address;
    private List<String> roles;
    private String userType;
    private String status;
    private String clientAdminId; // For users associated with a specific client admin
    private String mspId; // For users associated with a specific MSP
    private String department;
    private String profilePicture;
    private String updateBy;
}

