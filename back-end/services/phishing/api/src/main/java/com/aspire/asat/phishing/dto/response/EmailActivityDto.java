package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for email activity log entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailActivityDto {

    private String activityId;
    private String campaignId;
    private String campaignName;
    private String recipientId;
    private String recipientEmail;
    private String recipientName;

    private ActivityType activityType;
    private String activityLabel;
    private Instant timestamp;

    private String ipAddress;
    private String geoLocation;
    private String deviceType;
    private String browser;
}
