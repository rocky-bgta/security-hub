package com.aspire.asat.registration.data.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user status counts from dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusCountResponseDto {
    
    private Long totalActive;
    private Long totalInactive;
    private Long totalSuspended;
}

