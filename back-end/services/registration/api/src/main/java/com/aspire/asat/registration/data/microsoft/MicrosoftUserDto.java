package com.aspire.asat.registration.data.microsoft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicrosoftUserDto {
    private String id;
    private String displayName;
    private String email;
    private String userPrincipalName;
    private String givenName;
    private String surname;
    private String jobTitle;
    private String department;
    private String officeLocation;
    private String mobilePhone;
    private boolean accountEnabled;
}

