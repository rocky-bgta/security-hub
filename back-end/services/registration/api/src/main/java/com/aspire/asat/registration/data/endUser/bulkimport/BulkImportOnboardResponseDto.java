package com.aspire.asat.registration.data.endUser.bulkimport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportOnboardResponseDto {

    private int totalUsers;
    private int successful;
    private int failed;
    private List<BulkImportFailedUserDto> failedUsers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImportFailedUserDto {
        private String email;
        private String fullName;
        private String reason;
    }
}
