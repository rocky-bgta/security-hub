package com.aspire.asat.registration.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for new user notification to admin parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserNotificationDto {
    
    private String userEmail;
    private String userId;
    private String userName;
    private Instant registrationDate;
    private String adminName;
    private String adminEmail;
    private String clientAdminId; // Optional: for client-specific notification settings
}
