package com.aspire.asat.registration.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk user import summary notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportSummaryDto {
    
    private String adminEmail;
    private String adminName;
    private String adminId;
    private String clientAdminId; // Optional: for client-specific notification settings (admin's clientAdminId)
    private int totalImported;
    private int totalSkipped;
    private List<ImportedUserInfo> importedUsers;
    private List<SkippedUserInfo> skippedUsers;
    private String importDate;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportedUserInfo {
        private String email;
        private String fullName;
        private String phoneNumber;
        private String department;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkippedUserInfo {
        private String email;
        private String fullName;
        private String reason; // "duplicate" or "invalid_country"
    }
}
