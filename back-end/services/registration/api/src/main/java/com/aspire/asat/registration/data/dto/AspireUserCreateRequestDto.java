package com.aspire.asat.registration.data.dto;

import com.aspire.asat.registration.data.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new AspireUser
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AspireUserCreateRequestDto {

    @NotBlank(message = "Base user ID is required")
    private UUID baseUserId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phoneNumber;
    private String phoneCode;
    private String country;
    private String countryCode;
    private String address;

    @NotNull(message = "Roles are required")
    private List<String> roles;

    @NotBlank(message = "User type is required")
    private String userType;

    @NotBlank(message = "Status is required")
    private String status;

    private String createdBy; // ID of the user who created this user

    private String plainPassword; // Plain password for the newly created user
    /**
     * When true, password policy validation is skipped (e.g. sync user, system-generated temp password, superadmin init).
     * When false or null, user-provided passwords are validated.
     */
    private Boolean skipPasswordValidation;
    private String clientAdminId; // For users associated with a specific client admin
    private String mspId; // For users associated with a specific MSP
    private String department;
    private String companyName;
    private String designation; // Job designation/title
    private String supervisorName; // Name of the supervisor
    private RiskGroup riskGroup; // Risk group classification for the user
    private String profilePicture;
    private Boolean isCredentialSent; // Flag to indicate if credentials email should be sent

    /**
     * When true, user must change password on first login (temp/system-generated password).
     * Only set for MSP / CLIENT_ADMIN / ASPIRE_ADMIN / SUPER_ADMIN create paths.
     */
    private Boolean isDefault;
}

