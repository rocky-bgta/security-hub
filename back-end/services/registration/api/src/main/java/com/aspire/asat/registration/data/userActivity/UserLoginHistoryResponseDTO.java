package com.aspire.asat.registration.data.userActivity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for User Login History
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistoryResponseDTO {

    /**
     * Username of the user
     */
    private String username;

    /**
     * Action type (LOGIN or LOGOUT)
     */
    private String actionType;

    /**
     * Timestamp of the action
     */
    private Instant timestamp;

    /**
     * Device information from the request
     */
    private String deviceInfo;

    /**
     * User type (USER, SYSTEM_USER, etc.)
     */
    private String userType;

    /**
     * Request IP address
     */
    private String requestIp;
}
