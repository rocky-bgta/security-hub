package com.aspire.asat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing user data with client admin information
 * Used for notification purposes and other user-related operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDto {
    
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String clientAdminId;
    private String clientAdminName;
    private String clientAdminEmail;
}

