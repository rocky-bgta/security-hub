package com.aspire.asat.registration.data.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EndUserUpdateRequestDTO {

    @NotBlank(message = "User ID is required")
    private String id;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String department;

    private String countryCode;

    private String profilePicture;

    private String status;

}
