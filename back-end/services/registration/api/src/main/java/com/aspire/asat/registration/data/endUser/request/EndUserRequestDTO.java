package com.aspire.asat.registration.data.endUser.request;

import com.aspire.asat.registration.data.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EndUserRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName; // Optional: Some users may not have a last name

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    private String countryCode;

    @NotBlank(message = "Department is required")
    private String department;

    private UserStatus status; //ACTIVE, INACTIVE

    @NotBlank(message = "Client Admin ID is required")
    private String clientAdminId;

    private String mspId; // Optional: If the request comes from an MSP context
}
