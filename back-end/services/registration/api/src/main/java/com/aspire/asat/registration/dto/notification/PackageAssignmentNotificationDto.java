package com.aspire.asat.registration.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for package assignment notification parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageAssignmentNotificationDto {
    
    private String userEmail;
    private String userId;
    private String userName;
    private String packageName;
    private String packageDetails;
    private String assignmentDate;
    private String expirationDate;
    private String adminEmail;
    private String adminName;
    private String adminId;
    private String departmentName;
    private String companyName;
    private String logoUrl;
    private String clientAdminId; // Optional: for client-specific notification settings (admin's clientAdminId)
    private String tempPassword; // Optional: temporary password if applicable
}
